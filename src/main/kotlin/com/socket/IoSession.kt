package com.socket

import com.util.IOUtils
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.*
import io.netty.util.Attribute
import io.netty.util.AttributeKey
import java.util.function.Consumer

/**
 * 通信会话包装
 */
class IoSession(
    private val channelHandlerContext: ChannelHandlerContext,
    protected var actionEventManager: ActionEventManager
) {
    /**
     * 发送消息并获得成功监听
     * @param msg
     * @return
     */
    fun <T : Any> writeFlush(msg: T): ChannelFuture {
        val future = writeAndFlush(msg)
        if (future.isSuccess) {
            future.addListener { _ ->
                actionEventManager.getListeners().forEach {
                    IOUtils.getSession(future.channel())?.let { session ->
                        it.messageSent(session, msg)
                    }
                }
            }
        }
        return future
    }

    /**
     * 直接发送消息不监听成功与否
     * @param msg
     * @return
     */
    fun writeAndFlush(msg: Any?): ChannelFuture {
        return channel().writeAndFlush(msg)
    }

    fun write(msg: Any?): ChannelFuture {
        return channel().write(msg)
    }

    fun flush(): Channel {
        return channel().flush()
    }

    fun channel(): Channel {
        return channelHandlerContext.channel()
    }

    fun close(): ChannelFuture {
        return channel().close()
    }

    fun closeFuture(): ChannelFuture {
        return channel().closeFuture()
    }

    fun id(): ChannelId {
        return channel().id()
    }

    val isActive: Boolean
        get() = channel().isActive

    fun alloc(): ByteBufAllocator {
        return channel().alloc()
    }

    val isWritable: Boolean
        get() = channel().isWritable
    val isOpen: Boolean
        get() = channel().isOpen

    fun <T> attr(key: AttributeKey<T>): Attribute<T> {
        return channel().attr(key)
    }

    fun <T> hasAttr(key: AttributeKey<T>): Boolean {
        return channel().hasAttr(key)
    }

    fun pipeline(): ChannelPipeline {
        return channel().pipeline()
    }
}
