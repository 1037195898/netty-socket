package com.parse;

import com.entity.GameInput;
import com.socket.ActionData;
import com.util.IOUtils;
import com.util.ZlibUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class WebSocketDecoder extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LoggerFactory.getLogger(getClass()).debug("WebSocketDecoder.channelRead : " + msg);
        channelReadT(ctx, msg);
    }

    private void channelReadT(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TextWebSocketFrame) {
            String content = ((TextWebSocketFrame) msg).text();
            ActionData<?> data = new ActionData<>(-100);
            data.setBuf(content.getBytes(StandardCharsets.UTF_8));
            ctx.fireChannelRead(data);
        } if (msg instanceof BinaryWebSocketFrame) {
            ByteBuf byteBuf = Unpooled.copiedBuffer( ((BinaryWebSocketFrame) msg).content() );
            byte[] bytes = ByteBufUtil.getBytes(byteBuf);
            // 解压
            byte[] by = ZlibUtil.decompress(bytes);
            // 解密
            by = IOUtils.getAes(ctx.channel()).decrypt(by);
            GameInput input = new GameInput(by);
            ActionData<?> data = new ActionData<>(0);
//        System.out.println("事件头="+data.getAction());
//        System.out.println("获取了事件头后剩余的="+input.available());
//        System.out.println("获取包头后的长度,"+input.available()+", "+buf.remaining());
            data.setVerify(input.readLong());
            data.setAction(input.readInt());
            int byteLen = input.readInt();// 获取长度
            bytes = new byte[byteLen];
            input.read(bytes, 0, byteLen);
            data.setBuf(bytes);
//        System.out.println("获取所有数据后的长度,"+input.available()+", "+buf.remaining());
//        System.out.println(data.getData());
            ctx.fireChannelRead(data);
        }
        ReferenceCountUtil.release(msg);
    }

}
