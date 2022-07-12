package com.adapter;

import com.socket.ActionData;
import com.socket.ActionEventManager;
import com.socket.IoSession;
import com.util.IOUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BaseChannelAdapter<T> extends SimpleChannelInboundHandler<T> {

    protected ActionEventManager actionEventManager;

    public BaseChannelAdapter(ActionEventManager actionEventManager) {
        this.actionEventManager = actionEventManager;
    }

    /**
     * 新的客户端连接事件
     * @param ctx 通道
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        IoSession session = IOUtils.addSession(ctx, actionEventManager);
        LoggerFactory.getLogger(getClass()).debug("新建连接");
        actionEventManager.getListeners().forEach(sessionListener -> sessionListener.sessionCreated(session));
    }

    /**
     * 处理器移除事件(断开连接)
     * @param ctx 通道
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        LoggerFactory.getLogger(getClass()).debug("断开连接");
        actionEventManager.getIosIdle().remove(ctx.channel().id().asLongText());
        actionEventManager.getListeners().forEach(sessionListener -> sessionListener.sessionClosed(IOUtils.getSession(ctx)));
    }

    /**
     * 通道激活时触发，当客户端connect成功后，服务端就会接收到这个事件，从而可以把客户端的Channel记录下来，供后面复用
     * @param ctx 通道
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        LoggerFactory.getLogger(getClass()).debug("channelActive");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        super.userEventTriggered(ctx, evt);
//        System.out.println(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss") + ", userEventTriggered|" + evt);
        if (evt instanceof IdleStateEvent) { // 空闲状态
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.ALL_IDLE) {
                String id = ctx.channel().id().asLongText();
                Map<String, Integer> iosIdle = actionEventManager.getIosIdle();
                if (iosIdle.containsKey(id)) {
                    Integer count = iosIdle.get(id);
                    if (count < 3) {
                        count++;
                        ctx.writeAndFlush(new ActionData<>(1));
                        iosIdle.put(id, count);
                    } else {
                        iosIdle.remove(id);
                        ctx.channel().close();
                        LoggerFactory.getLogger(getClass()).info("有用户空闲被关闭=[" + id + " , 用户数据=" + ctx + "]");
                    }
                } else {
                    iosIdle.put(id, 1);
                    ctx.writeAndFlush(new ActionData<>(1));
                }
            }
            actionEventManager.getListeners().forEach(sessionListener -> sessionListener.sessionIdle(IOUtils.getSession(ctx), e.state()));
        } else if (evt instanceof WebSocketClientProtocolHandler.ClientHandshakeStateEvent) {
            if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                actionEventManager.getListeners().forEach(sessionListener -> sessionListener.handshakeComplete(IOUtils.getSession(ctx)));
            }
        } else if (evt instanceof WebSocketServerProtocolHandler.ServerHandshakeStateEvent) {
            if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                actionEventManager.getListeners().forEach(sessionListener -> sessionListener.handshakeComplete(IOUtils.getSession(ctx)));
            }
        }
        // 执行父类的方法
        ctx.fireUserEventTriggered(evt);
    }

    /**
     * 当收到对方发来的数据后，就会触发，参数msg就是发来的信息，可以是基础类型，也可以是序列化的复杂对象。
     * @param ctx 通道
     * @param msg 数据
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        actionEventManager.getListeners().forEach(sessionListener -> sessionListener.messageReceived(IOUtils.getSession(ctx), msg));
        actionEventManager.getIosIdle().remove(ctx.channel().id().asLongText());
    }

}
