package com.web;

import com.adapter.MessageAdapter;
import com.entity.GameInput;
import com.socket.ActionData;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

@ChannelHandler.Sharable
public class WebChannelAdapter extends MessageAdapter {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ActionData<?> msg) throws Exception {
//        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer("我来了".getBytes())));
        LoggerFactory.getLogger(getClass()).debug("channelRead0:" + msg);
        if (msg.getAction() == -100) {
            LoggerFactory.getLogger(getClass()).debug("channelRead0:" + StringUtils.toEncodedString(msg.getBuf(), Charset.defaultCharset()));
        } else {
            GameInput gameInput = new GameInput(msg.getBuf());
            LoggerFactory.getLogger(getClass()).debug("channelRead0:" + gameInput.readUTF());
        }
    }

}