package com.adapter

import com.socket.ActionData
import com.socket.ActionEventManager
import com.util.ActionUtils.run
import com.util.IOUtils
import com.util.PoolUtils.returnObject
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * 带有派发事件功能的监听器
 */
@Sharable
class MessageAdapter(actionEventManager: ActionEventManager) : BaseChannelAdapter<ActionData<Any>>(actionEventManager) {

    val sessionVerify = mutableMapOf<String, Long>()

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        super.handlerAdded(ctx)
        sessionVerify[ctx.channel().id().asLongText()] = 0L
    }

    /**
     * 处理器移除事件(断开连接)
     *
     * @param ctx
     * @throws Exception
     */
    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        super.handlerRemoved(ctx)
        sessionVerify.remove(ctx.channel().id().asLongText())
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ActionData<Any>) {
        super.channelRead0(ctx, msg)
        //        if (sessionVerify.containsKey(ctx.channel().id().asLongText())
//                && msg.getVerify() > sessionVerify.get(ctx.channel().id().asLongText())) {
//            sessionVerify.put(ctx.channel().id().asLongText(), msg.getVerify());

        IOUtils.getSession(ctx)?.let {
            val result = run(msg.action, msg, it)
            actionEventManager.executeActionMapping(msg, it, msg.buf, result)
            returnObject(msg)

        }
        //        }
    }

    /**
     * 异常发生事件
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
//        super.exceptionCaught(ctx, cause)
//        logger.error("client caught exception", cause)
        if (cause is IOException) {
            actionEventManager.iosIdle.remove(ctx.channel().id().asLongText())
            sessionVerify.remove(ctx.channel().id().asLongText())
        }
        actionEventManager.getListeners().forEach { sessionListener ->
            IOUtils.getSession(ctx)?.let { sessionListener.exceptionCaught(it, cause) }
        }
        ctx.close()
        LoggerFactory.getLogger(javaClass).debug("client caught exception", cause)
    }

}
