package com.parse

import com.entity.Output
import com.socket.ActionData
import com.util.IOUtils.aes
import com.util.PoolUtils.getObject
import com.util.PoolUtils.returnObject
import com.util.ZlibUtil.compress
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.MessageToByteEncoder

@Sharable
class MessageEncoder(
    /**
     * 是否加密
     */
    private var isEncrypt: Boolean = false
) : MessageToByteEncoder<ActionData<*>>() {
    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, msg: ActionData<*>, out: ByteBuf) {
        val gameOutput = getObject(Output::class.java)
        try {
            gameOutput.writeLong(System.currentTimeMillis()) // 发送当前服务器的时间
            gameOutput.writeInt(msg.action)
            var buf = msg.buf
            buf = buf ?: ByteArray(0)
            gameOutput.writeInt(buf.size)
            gameOutput.write(buf, 0, buf.size)
            var bytes = gameOutput.toByteArray()
            // 加密
//			System.out.println(bytes.length);
            val str = aes.encrypt(bytes)
            gameOutput.reset()
            bytes = str.toByteArray()
            // 压缩
            bytes = compress(bytes)
            out.writeInt(bytes.size)
            out.writeBytes(bytes)
        } finally {
            returnObject(gameOutput)
        }
    }

    override fun close(ctx: ChannelHandlerContext, promise: ChannelPromise) {
        super.close(ctx, promise)
    }

    companion object {

        val inst by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { MessageEncoder() }
        fun inst(isEncrypt: Boolean): MessageEncoder {
            return inst.also { it.isEncrypt = isEncrypt }
        }

    }
}
