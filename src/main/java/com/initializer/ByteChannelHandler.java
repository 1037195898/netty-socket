package com.initializer;

import com.parse.MessageDecoder;
import com.parse.MessageEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class ByteChannelHandler extends ChannelInitializer<Channel> {

    private ChannelHandler[] channelHandler;

    public ByteChannelHandler(ChannelHandler ...channelHandler) {
        this.channelHandler = channelHandler;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        //此方法每次客户端连接都会调用，是为通道初始化的方法
        //获得通道channel中的管道链（执行链、handler链）
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new MessageDecoder()); // 解析不能使用共享  因为有粘包功能需要独立
        pipeline.addLast(MessageEncoder.getInst()); // 使用单例 节约创建类
        // 添加自定义的Handler
        for (int i = 0; i < channelHandler.length; i++) {
            ChannelHandler handler = channelHandler[i];
            if (handler instanceof IdleStateHandler) {
                initIdle(pipeline, (IdleStateHandler) handler);
            } else {
                pipeline.addLast("handler" + i, handler);
            }
        }
    }

    private void initIdle(ChannelPipeline pipeline, IdleStateHandler handler) {
        pipeline.addLast(new IdleStateHandler(
                handler.getReaderIdleTimeInMillis(),
                handler.getWriterIdleTimeInMillis(),
                handler.getAllIdleTimeInMillis(),
                TimeUnit.MILLISECONDS
        ));
    }

}
