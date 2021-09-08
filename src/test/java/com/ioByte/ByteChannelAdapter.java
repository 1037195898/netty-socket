package com.ioByte;

import com.adapter.MessageAdapter;
import com.socket.ActionData;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class ByteChannelAdapter extends MessageAdapter {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ActionData<?> msg) throws Exception {
        super.channelRead0(ctx, msg);



    }

}
