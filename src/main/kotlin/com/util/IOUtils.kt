package com.util

import com.decoder.AES
import com.enums.IoName
import com.socket.ActionEventManager
import com.socket.IoSession
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.util.AttributeKey
import kotlin.concurrent.getOrSet

object IOUtils {

    private val threadLocal = ThreadLocal<AES>()
    /**
     * 获取当前线程保存的 aes
     * @return AES
     */
    @JvmStatic
    val aes: AES
        get() = threadLocal.getOrSet { AES() }

    /**
     * 获取渠道中的包装器
     * @param channel 渠道
     * @return ioSession
     */
    fun getSession(channel: ChannelHandlerContext): IoSession? {
        return getSession(channel.channel())
    }

    /**
     * 获取渠道中的包装器
     * @param channel 渠道
     * @return ioSession
     */
    fun getSession(channel: Channel): IoSession? {
        val attributeKey = AttributeKey.valueOf<IoSession>(IoName.SESSION.name)
        var session: IoSession? = null
        if (channel.hasAttr(attributeKey)) {
            session = channel.attr(attributeKey).get()
        }
        return session
    }

    fun addSession(ctx: ChannelHandlerContext, actionEventManager: ActionEventManager): IoSession {
        val attributeKey = AttributeKey.valueOf<IoSession>(IoName.SESSION.name)
        val session: IoSession
        if (!ctx.channel().hasAttr(attributeKey)) {
            session = IoSession(ctx, actionEventManager)
            ctx.channel().attr(attributeKey).set(session)
        } else {
            session = ctx.channel().attr(attributeKey).get()
        }
        return session
    }
}
