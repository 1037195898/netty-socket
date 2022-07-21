package com.util;

import com.decoder.AES;
import com.enums.IoName;
import com.socket.ActionEventManager;
import com.socket.IoSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class IOUtils {

    private static final ThreadLocal<AES> threadLocal = new ThreadLocal<>();

    /**
     * 获取当前线程保存的 aes
     * @return AES
     */
    public static AES getAes() {
        AES aes = threadLocal.get();
        if (aes == null) {
            aes = new AES();
            threadLocal.set(aes);
        }
        return aes;
    }

    /**
     * 获取渠道中的包装器
     * @param channel 渠道
     * @return ioSession
     */
    public static IoSession getSession(ChannelHandlerContext channel) {
        return getSession(channel.channel());
    }

    /**
     * 获取渠道中的包装器
     * @param channel 渠道
     * @return ioSession
     */
    public static IoSession getSession(Channel channel) {
        AttributeKey<IoSession> attributeKey = AttributeKey.valueOf(IoName.SESSION.name());
        IoSession session = null;
        if (channel.hasAttr(attributeKey)) {
            session = channel.attr(attributeKey).get();
        }
        return session;
    }

    public static IoSession addSession(ChannelHandlerContext ctx, ActionEventManager actionEventManager) {
        AttributeKey<IoSession> attributeKey = AttributeKey.valueOf(IoName.SESSION.name());
        IoSession session;
        if (!ctx.channel().hasAttr(attributeKey)) {
            session = new IoSession(ctx, actionEventManager);
            ctx.channel().attr(attributeKey).set(session);
        } else {
            session = ctx.channel().attr(attributeKey).get();
        }
        return session;
    }

}
