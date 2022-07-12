package com.socket;

import com.util.IOUtils;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

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
                                sessionListener.messageSent(IOUtils.getSession(future.channel()), msg));
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

    public ChannelFuture close() {
        return channel().close();
    }
    public ChannelFuture closeFuture() {
        return channel().closeFuture();
    }
    public ChannelId id() {
        return channel().id();
    }
    public boolean isActive() {
        return channel().isActive();
    }
    public ByteBufAllocator alloc() {
        return channel().alloc();
    }
    public boolean isWritable() {
        return channel().isWritable();
    }
    public boolean isOpen() {
        return channel().isOpen();
    }
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return channel().attr(key);
    }
    public <T> boolean hasAttr(AttributeKey<T> key) {
        return channel().hasAttr(key);
    }
    public ChannelPipeline pipeline() {
        return channel().pipeline();
    }

}
