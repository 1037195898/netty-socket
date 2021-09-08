package com.adapter;

import com.socket.ActionData;
import com.util.ActionUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 带有派发事件功能的监听器
 */
@ChannelHandler.Sharable
public class MessageAdapter extends BaseChannelAdapter<ActionData<?>> {

    protected Map<String, Long> sessionVerify = new HashMap<>();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        sessionVerify.put(ctx.channel().id().asLongText(), (long) 0);
    }

    /**
     * 处理器移除事件(断开连接)
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        sessionVerify.remove(ctx.channel().id().asLongText());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ActionData<?> msg) throws Exception {
        super.channelRead0(ctx, msg);
        if (sessionVerify.containsKey(ctx.channel().id().asLongText())
                && msg.getVerify() > sessionVerify.get(ctx.channel().id().asLongText())) {
            sessionVerify.put(ctx.channel().id().asLongText(), msg.getVerify());
            ActionUtils.getInst().executeActionMapping(msg, ctx, msg);
        }
    }

    /**
     * 异常发生事件
     * @param ctx
     * @param cause
     * @throws Exception
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        logger.error("client caught exception", cause);
        if (cause instanceof IOException) {
            ActionUtils.getInst().getIosIdle().remove(ctx.channel().id().asLongText());
            sessionVerify.remove(ctx.channel().id().asLongText());
        }
        ActionUtils.getInst().getListeners().forEach(sessionListener -> sessionListener.exceptionCaught(ctx, cause));
        ctx.close();
    }

    public Map<String, Long> getSessionVerify() {
        return sessionVerify;
    }

}
