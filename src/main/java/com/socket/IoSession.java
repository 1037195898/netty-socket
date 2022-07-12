package com.socket;

import io.netty.channel.*;

/**
 * 通信会话包装
 */
public class IoSession {

    private ChannelHandlerContext channelHandlerContext;
    protected ActionEventManager actionEventManager;

    public IoSession(ChannelHandlerContext channelHandlerContext, ActionEventManager actionEventManager) {
        this.channelHandlerContext = channelHandlerContext;
        this.actionEventManager = actionEventManager;
    }

    /**
     * 发送消息并获得成功监听
     * @param msg
     * @return
     */
    public ChannelFuture writeFlush(Object msg) {
        ChannelFuture future = writeAndFlush(msg);
        if (future.isSuccess()) {
            future.addListener((ChannelFutureListener) future1 -> {
                actionEventManager.getListeners()
                        .forEach(sessionListener ->
                                sessionListener.messageSent(msg));
            });
        }
        return future;
    }

    /**
     * 直接发送消息不监听成功与否
     * @param msg
     * @return
     */
    public ChannelFuture writeAndFlush(Object msg) {
        return channel().writeAndFlush(msg);
    }

    public ChannelFuture write(Object msg) {
        return channel().write(msg);
    }

    public Channel flush() {
        return channel().flush();
    }

    public Channel channel() {
        return channelHandlerContext.channel();
    }

}
