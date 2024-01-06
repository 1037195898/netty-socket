package com.adapter

import com.socket.ActionData
import com.socket.ActionEventManager
import com.socket.SessionListener
import com.util.IOUtils
import com.util.PoolUtils.getObject
import com.util.SocketUtils
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.ServerHandshakeStateEvent
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import org.slf4j.LoggerFactory
import java.util.function.Consumer

open class BaseChannelAdapter<T : Any>(protected var actionEventManager: ActionEventManager) : SimpleChannelInboundHandler<T>() {
    /**
     * 新的客户端连接事件
     * @param ctx 通道
     */
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        super.handlerAdded(ctx)
        val session = IOUtils.addSession(ctx, actionEventManager)
        LoggerFactory.getLogger(javaClass).debug("新建连接")
        actionEventManager.getListeners()
            .forEach(Consumer { sessionListener -> sessionListener.sessionCreated(session) })
    }

    /**
     * 处理器移除事件(断开连接)
     * @param ctx 通道
     */
    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        super.handlerRemoved(ctx)
        LoggerFactory.getLogger(javaClass).debug("断开连接")
        actionEventManager.iosIdle.remove(ctx.channel().id().asLongText())
        actionEventManager.getListeners().forEach { sessionListener ->
            IOUtils.getSession(ctx)?.let {
                sessionListener.sessionClosed(it)
            }
        }
    }

    /**
     * 通道激活时触发，当客户端connect成功后，服务端就会接收到这个事件，从而可以把客户端的Channel记录下来，供后面复用
     * @param ctx 通道
     */
    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        LoggerFactory.getLogger(javaClass).debug("channelActive")
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
//        super.userEventTriggered(ctx, evt);
//        System.out.println(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss") + ", userEventTriggered|" + evt);
        if (evt is IdleStateEvent) { // 空闲状态
            val e = evt
            if (e.state() == IdleState.ALL_IDLE) {
                val id = ctx.channel().id().asLongText()
                val iosIdle = actionEventManager.iosIdle
                if (iosIdle.containsKey(id)) {
                    var count = iosIdle[id]!!
                    if (count < 3) {
                        count += 1
                        ctx.writeAndFlush(SocketUtils.DEFAULT_IDLE_ACTION.also {
                            getObject(ActionData::class.java).action = it
                        })
                        iosIdle[id] = count
                    } else {
                        iosIdle.remove(id)
                        ctx.channel().close()
                        LoggerFactory.getLogger(javaClass).info("有用户空闲被关闭=[$id , 用户数据=$ctx]")
                    }
                } else {
                    iosIdle[id] = 1
                    ctx.writeAndFlush(SocketUtils.DEFAULT_IDLE_ACTION.also {
                        getObject(ActionData::class.java).action = it
                    })
                }
            }
            actionEventManager.getListeners().forEach { session ->
                IOUtils.getSession(ctx)?.let { session.sessionIdle(it, e.state()) }
            }
        } else if (evt is ClientHandshakeStateEvent) {
            if (evt === ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                actionEventManager.getListeners().forEach { session ->
                    IOUtils.getSession(ctx)?.let { session.handshakeComplete(it) }
                }
            }
        } else if (evt is ServerHandshakeStateEvent) {
            if (evt === ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                actionEventManager.getListeners().forEach { session ->
                    IOUtils.getSession(ctx)?.let { session.handshakeComplete(it) }
                }
            }
        }
        // 执行父类的方法
        ctx.fireUserEventTriggered(evt)
    }

    /**
     * 当收到对方发来的数据后，就会触发，参数msg就是发来的信息，可以是基础类型，也可以是序列化的复杂对象。
     * @param ctx 通道
     * @param msg 数据
     */
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        super.channelRead(ctx, msg)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: T) {
        actionEventManager.getListeners().forEach { sessionListener ->
            IOUtils.getSession(ctx)?.let { sessionListener.messageReceived(it, msg) }
        }
        actionEventManager.iosIdle.remove(ctx.channel().id().asLongText())
    }

}
