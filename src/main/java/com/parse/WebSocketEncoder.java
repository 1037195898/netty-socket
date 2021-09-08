package com.parse;

import com.entity.GameOutput;
import com.socket.ActionData;
import com.util.IOUtils;
import com.util.ZlibUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.LoggerFactory;

import java.util.List;

@ChannelHandler.Sharable
public class WebSocketEncoder extends MessageToMessageEncoder<ActionData<?>> {

    private static WebSocketEncoder encoder;
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

    @Override
    protected void encode(ChannelHandlerContext ctx, ActionData<?> msg, List<Object> out) throws Exception {
        GameOutput gameOutput = new GameOutput();
        try {
            gameOutput.writeLong(System.currentTimeMillis());// 发送当前服务器的时间
            gameOutput.writeInt(msg.getAction());
            byte[] buf = msg.getBuf();
            buf = buf == null ? new byte[0] : buf;
            gameOutput.writeInt(buf.length);
            gameOutput.write(buf, 0, buf.length);
            byte[] bytes = gameOutput.toByteArray();
            // 加密
//			System.out.println(bytes.length);
            String str = IOUtils.getAes(ctx.channel()).encrypt(bytes);
            gameOutput.reset();
            bytes = str.getBytes();
            // 压缩
            bytes = ZlibUtil.compress(bytes);
            LoggerFactory.getLogger(getClass()).debug("发送数据：" + bytes.length);
            out.add(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)));
//            TextWebSocketFrame
//            BinaryWebSocketFrame
        } finally {
            gameOutput.close();
        }
    }

}
