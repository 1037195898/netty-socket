package com.parse;

import com.entity.GameInput;
import com.socket.ActionData;
import com.util.IOUtils;
import com.util.ZlibUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

public class TestMessageEncoder extends MessageToMessageEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        System.out.println(msg);
        if (msg instanceof TextWebSocketFrame) {
            System.out.println(((TextWebSocketFrame) msg).text());
        } if (msg instanceof BinaryWebSocketFrame) {

        }
    }
}
