package com.parse

import com.entity.Input
import com.socket.ActionData
import com.util.IOUtils.aes
import com.util.PoolUtils.getObject
import com.util.SocketType
import com.util.SocketUtils
import com.util.ZlibUtil.decompress
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.util.ReferenceCountUtil
import org.slf4j.LoggerFactory

/**
 * 解析收到的数据
 */
@Sharable
class WebSocketDecoder(
    /**
     * 是否加密
     */
    var isEncrypt: Boolean = false
) : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        LoggerFactory.getLogger(javaClass).debug("WebSocketDecoder.channelRead : {}", msg)
        channelReadT(ctx, msg)
    }

    private fun channelReadT(ctx: ChannelHandlerContext, msg: Any) {
        var byteBuf: ByteBuf? = null
        if (msg is TextWebSocketFrame) {
            byteBuf = msg.content()
        } else if (msg is BinaryWebSocketFrame) {
            byteBuf = Unpooled.copiedBuffer(msg.content())
        }
        if (byteBuf == null) {
            ctx.fireChannelRead(msg)
            return
        }
        var bytes = ByteBufUtil.getBytes(byteBuf)
        if (isEncrypt) {
            // 解压
            bytes = decompress(bytes)
            // 解密
            bytes = aes.decrypt(bytes)
        }
//        println(Thread.currentThread().name + "|decoder")
        val data = getObject(ActionData::class.java)
        //        System.out.println("事件头="+data.getAction());
//        System.out.println("获取了事件头后剩余的="+input.available());
//        System.out.println("获取包头后的长度,"+input.available()+", "+buf.remaining());
        if (SocketUtils.webSocketType == SocketType.TEXT_WEB_SOCKET_FRAME) {
            data.verify = System.currentTimeMillis()
            data.buf = bytes
        } else {
            val input = Input(bytes)
            data.verify = input.readLong()
            data.action = input.readInt()
            val byteLen = input.readInt() // 获取长度
            bytes = ByteArray(byteLen)
            input.read(bytes, 0, byteLen)
            data.buf = bytes
        }
        //        System.out.println("获取所有数据后的长度,"+input.available()+", "+buf.remaining());
//        System.out.println(data.getData());
        ctx.fireChannelRead(data)
        ReferenceCountUtil.release(msg)
    }

    companion object {

        val inst by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { WebSocketDecoder() }
        fun inst(isEncrypt: Boolean): WebSocketDecoder {
            return inst.also { it.isEncrypt = isEncrypt }
        }

    }
}
