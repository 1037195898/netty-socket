package com.parse;

import com.entity.GameOutput;
import com.socket.ActionData;
import com.util.IOUtils;
import com.util.ZlibUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<ActionData<?>> {

    private static MessageEncoder encoder;
    public static MessageEncoder getInst() {
        if (encoder == null) {
            synchronized (MessageEncoder.class) {
                if (encoder == null) {
                    encoder = new MessageEncoder();
                }
            }
        }
        return encoder;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ActionData msg, ByteBuf out) throws Exception {
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
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
        } finally {
            gameOutput.close();
        }
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
    }

}
