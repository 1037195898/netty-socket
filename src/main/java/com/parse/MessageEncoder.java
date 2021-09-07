package com.parse;

import com.decoder.AES;
import com.entity.GameOutput;
import com.socket.ActionData;
import com.util.ZlibUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.AttributeKey;

public class MessageEncoder extends MessageToByteEncoder<ActionData<?>> {

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
            String str = getAes(ctx.channel()).encrypt(bytes);
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

    /**
     * 获取对象保存的 aes
     *
     * @param channel 渠道
     * @return AES
     */
    protected AES getAes(Channel channel) {
        AttributeKey<AES> attributeKey = AttributeKey.valueOf("key_" + channel.id());
        AES aes;
        if (!channel.hasAttr(attributeKey)) {
            aes = new AES();
            channel.attr(attributeKey).set(aes);
        } else {
            aes = channel.attr(attributeKey).get();
        }
        return aes;
    }

}
