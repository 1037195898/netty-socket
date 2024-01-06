package com.parse

import com.entity.Output
import com.socket.ActionData
import com.util.IOUtils.aes
import com.util.PoolUtils.getObject
import com.util.PoolUtils.returnObject
import com.util.SocketType
import com.util.SocketUtils
import com.util.ZlibUtil.compress
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.slf4j.LoggerFactory
import kotlin.concurrent.Volatile

/**
 * 编译发送数据
 */
@Sharable
class WebSocketEncoder(
    /**
     * 是否加密
     */
    private var isEncrypt: Boolean = false
) : MessageToMessageEncoder<ActionData<*>>() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun encode(ctx: ChannelHandlerContext, msg: ActionData<*>, out: MutableList<Any>) {
        var buf = msg.buf
        if (SocketUtils.webSocketType == SocketType.TEXT_WEB_SOCKET_FRAME) {
            buf?.let {
                if (isEncrypt) {
                    // 加密
//			        System.out.println(bytes.length);
                    val str = aes.encrypt(it)
                    // 压缩
                    buf = compress(str.toByteArray())
                }
                out.add(TextWebSocketFrame(Unpooled.wrappedBuffer(buf)))
            }
        } else {
            val gameOutput = getObject(Output::class.java)
            runCatching {
                gameOutput.writeLong(System.currentTimeMillis()) // 发送当前服务器的时间
                gameOutput.writeInt(msg.action)
                buf?.let {
                    gameOutput.writeInt(it.size)
                    gameOutput.write(it, 0, it.size)
                } ?: gameOutput.writeInt(0)
                var bytes = gameOutput.toByteArray()
                if (isEncrypt) {
                    // 加密
//			        System.out.println(bytes.length);
                    val str = aes.encrypt(bytes)
                    gameOutput.reset()
                    bytes = str.toByteArray()
                    // 压缩
                    bytes = compress(bytes)
                }
                logger.debug("压缩否=" + isEncrypt + ", 格式=" + SocketUtils.webSocketType + ",发送数据：" + bytes.size)
                out.add(BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)))
            }.onFailure {
                logger.error("socket发送失败", it)
            }
            returnObject(gameOutput)
        }
    }

    companion object {

        val inst by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { WebSocketEncoder() }
        fun inst(isEncrypt: Boolean): WebSocketEncoder {
            return inst.also { it.isEncrypt = isEncrypt }
        }

    }
}
