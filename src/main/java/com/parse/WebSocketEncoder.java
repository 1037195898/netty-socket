package com.parse;

import com.entity.GameOutput;
import com.socket.ActionData;
import com.util.IOUtils;
import com.util.SocketType;
import com.util.SocketUtils;
import com.util.ZlibUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 编译发送数据
 */
@ChannelHandler.Sharable
public class WebSocketEncoder extends MessageToMessageEncoder<ActionData<?>> {

    /**
     * 是否加密
     */
    private boolean isEncrypt;
    private static volatile WebSocketEncoder encoder;

    public static WebSocketEncoder getInst() {
        if (encoder == null) {
            synchronized (WebSocketEncoder.class) {
                if (encoder == null) {
                    encoder = new WebSocketEncoder();
                }
            }
        }
        return encoder;
    }

    public static WebSocketEncoder getInst(boolean isEncrypt) {
        if (encoder == null) {
            synchronized (WebSocketEncoder.class) {
                if (encoder == null) {
                    encoder = new WebSocketEncoder();
                }
            }
        }
        encoder.isEncrypt = isEncrypt;
        return encoder;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ActionData<?> msg, List<Object> out) throws Exception {
        byte[] buf = msg.getBuf();
        if (SocketUtils.webSocketType == SocketType.TEXT_WEB_SOCKET_FRAME) {
            if (isEncrypt) {
                // 加密
//			        System.out.println(bytes.length);
                String str = IOUtils.getAes(ctx.channel()).encrypt(buf);
                buf = str.getBytes();
                // 压缩
                buf = ZlibUtil.compress(buf);
            }
            out.add(new TextWebSocketFrame(Unpooled.wrappedBuffer(buf)));
        } else {
            GameOutput gameOutput = new GameOutput();
            try {
                gameOutput.writeLong(System.currentTimeMillis());// 发送当前服务器的时间
                gameOutput.writeInt(msg.getAction());
                buf = buf == null ? new byte[0] : buf;
                gameOutput.writeInt(buf.length);
                gameOutput.write(buf, 0, buf.length);
                byte[] bytes = gameOutput.toByteArray();
                if (isEncrypt) {
                    // 加密
//			        System.out.println(bytes.length);
                    String str = IOUtils.getAes(ctx.channel()).encrypt(bytes);
                    gameOutput.reset();
                    bytes = str.getBytes();
                    // 压缩
                    bytes = ZlibUtil.compress(bytes);
                }
                LoggerFactory.getLogger(getClass()).debug("压缩否=" + isEncrypt + ", 格式=" + SocketUtils.webSocketType + ",发送数据：" + bytes.length);
                out.add(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)));
            } finally {
                gameOutput.close();
            }
        }
    }

}
