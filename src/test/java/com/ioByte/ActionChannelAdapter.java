package com.ioByte;

import com.adapter.BaseChannelAdapter;
import com.socket.ActionData;
import com.util.ActionUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class ActionChannelAdapter extends BaseChannelAdapter<ActionData<?>> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ActionData<?> msg) throws Exception {
        super.channelRead0(ctx, msg);
        if (sessionVerify.containsKey(ctx.channel().id().asLongText())
                && msg.getVerify() > sessionVerify.get(ctx.channel().id().asLongText())) {
            sessionVerify.put(ctx.channel().id().asLongText(), msg.getVerify());
            ActionUtils.getInst().executeActionMapping(msg, ctx, msg);
        }
    }

}
