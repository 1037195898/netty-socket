package com.parse

import com.entity.Input
import com.socket.ActionData
import com.util.IOUtils.aes
import com.util.PoolUtils.getObject
import com.util.ZlibUtil.decompress
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder

class MessageDecoder(
    maxFrameLength: Int = RECEIVE_MAX,
    /**
     * 是否加密
     */
    private var isEncrypt: Boolean = false
) : LengthFieldBasedFrameDecoder(maxFrameLength, 0, Integer.BYTES) {
    companion object {
        /**
         * 接收最大字节  2M
         */
        var RECEIVE_MAX: Int = 1024 * 1024 * 2

        val inst by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { MessageDecoder() }
        fun inst(isEncrypt: Boolean): MessageDecoder {
            return inst.also { it.isEncrypt = isEncrypt }
        }
    }

    override fun decode(ctx: ChannelHandlerContext?, byteBuf: ByteBuf?): Any? {
//        return super.decode(ctx, in);
        if (byteBuf == null) {
            return null
        }
        // 是否满足头的数据
        if (byteBuf.readableBytes() <= Integer.BYTES) {
            return null
        }
        byteBuf.markReaderIndex() // 标记
        val dataLength = byteBuf.readInt()
        // 可读数据不足
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex() // 还原到标记处
            return null
        }
        val byteBuf = byteBuf.readBytes(dataLength)
        var bytes = ByteBufUtil.getBytes(byteBuf)
        // 解压
        var by: ByteArray = decompress(bytes)
        // 解密
        by = aes.decrypt(by)
        val input = Input(by)
        val data = getObject(ActionData::class.java)
        //        System.out.println("事件头="+data.getAction());
//        System.out.println("获取了事件头后剩余的="+input.available());
//        System.out.println("获取包头后的长度,"+input.available()+", "+buf.remaining());
        data.verify = input.readLong()
        data.action = input.readInt()
        val byteLen = input.readInt() // 获取长度
        bytes = ByteArray(byteLen)
        input.read(bytes, 0, byteLen)
        data.buf = bytes
        //        System.out.println("获取所有数据后的长度,"+input.available()+", "+buf.remaining());
//        System.out.println(data.getData());
        return data
    }


}
