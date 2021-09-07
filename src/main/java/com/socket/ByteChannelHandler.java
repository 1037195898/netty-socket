package com.socket;

import com.parse.MessageDecoder;
import com.parse.MessageEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class ByteChannelHandler extends ChannelInitializer<Channel> {

    private IdleStateHandler idleStateHandler;
    private ActionChannelAdapter actionChannelAdapter;
    private List<ChannelHandler> handlers = new ArrayList<>();

    public ByteChannelHandler(ActionChannelAdapter actionChannelAdapter) {
        this.actionChannelAdapter = actionChannelAdapter;
    }

    public ByteChannelHandler(ActionChannelAdapter actionChannelAdapter, IdleStateHandler idleStateHandler) {
        this.actionChannelAdapter = actionChannelAdapter;
        this.idleStateHandler = idleStateHandler;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        //此方法每次客户端连接都会调用，是为通道初始化的方法
        //获得通道channel中的管道链（执行链、handler链）
        ChannelPipeline pipeline = channel.pipeline();
        for (ChannelHandler handler : handlers) {
            pipeline.addLast(handler);
        }
        if (idleStateHandler != null) {
            pipeline.addLast(new IdleStateHandler(
                    idleStateHandler.getReaderIdleTimeInMillis(),
                    idleStateHandler.getWriterIdleTimeInMillis(),
                    idleStateHandler.getAllIdleTimeInMillis(),
                    TimeUnit.MILLISECONDS
            ));
        }
        pipeline.addLast(new MessageDecoder());
        pipeline.addLast(new MessageEncoder());
        pipeline.addLast("handler", actionChannelAdapter);
    }

    public void addLast(ChannelHandler channelHandler) {
        handlers.add(channelHandler);
    }

}
