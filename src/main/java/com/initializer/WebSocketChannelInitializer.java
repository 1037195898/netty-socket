package com.initializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class WebSocketChannelInitializer extends ChannelInitializer<Channel> {

    /** 是否是服务器 */
    private final URI clientUri;
    private IPipeline iPipeline;
    private ChannelHandler[] channelHandler;

    public WebSocketChannelInitializer(ChannelHandler... channelHandler) {
        this(null, null, channelHandler);
    }

    public WebSocketChannelInitializer(IPipeline iPipeline, ChannelHandler[] channelHandler) {
        this(null, iPipeline, channelHandler);
    }

    /**
     *
     * @param clientUri 客户端使用的url
     * @param channelHandler
     */
    public WebSocketChannelInitializer(URI clientUri, ChannelHandler... channelHandler) {
        this(clientUri, null, channelHandler);
    }

    public WebSocketChannelInitializer(URI clientUri, IPipeline pipeline, ChannelHandler... channelHandler) {
        this.clientUri = clientUri;
        this.iPipeline = pipeline;
        this.channelHandler = channelHandler;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        //此方法每次客户端连接都会调用，是为通道初始化的方法
        //获得通道channel中的管道链（执行链、handler链）
        ChannelPipeline pipeline = channel.pipeline();
        // 添加一个http的编解码器
        if (clientUri == null) {
            pipeline.addLast("http-codec", new HttpServerCodec());
        } else {
            pipeline.addLast("http-codec", new HttpClientCodec());
        }
        // 添加一个用于支持大数据流的支持  分块传输
        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
        // 添加一个聚合器，这个聚合器主要是将HttpMessage聚合成FullHttpRequest/Response
        pipeline.addLast(new HttpObjectAggregator(1024 * 64));
        // 需要指定接收请求的路由    必须使用以ws后缀结尾的url才能访问
        if (clientUri == null) {
            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",
                    null, false, Integer.MAX_VALUE));
        } else {
            pipeline.addLast(new WebSocketClientProtocolHandler(
                    WebSocketClientHandshakerFactory.newHandshaker(clientUri, WebSocketVersion.V13,
                    null, false, new DefaultHttpHeaders(), Integer.MAX_VALUE)));
        }

//        pipeline.addLast(WebSocketDecoder.getInst(isEncrypt));// 使用单例 节约创建类
//        pipeline.addLast(WebSocketEncoder.getInst(isEncrypt));// 使用单例 节约创建类

        // 添加自定义的Handler
        for (int i = 0; i < channelHandler.length; i++) {
            ChannelHandler handler = channelHandler[i];
            if (handler instanceof IdleStateHandler) {
                initIdle(pipeline, (IdleStateHandler) handler);
            } else {
                pipeline.addLast("handler" + i, handler);
            }
        }

        if (iPipeline != null) {
            iPipeline.run(channel, pipeline);
        }

    }

    private void initIdle(ChannelPipeline pipeline, IdleStateHandler handler) {
        pipeline.addFirst(new IdleStateHandler(
                handler.getReaderIdleTimeInMillis(),
                handler.getWriterIdleTimeInMillis(),
                handler.getAllIdleTimeInMillis(),
                TimeUnit.MILLISECONDS
        ));
    }

    public interface IPipeline {

        /** 自动处理pipeline */
        void run(Channel channel, ChannelPipeline pipeline);

    }

}


