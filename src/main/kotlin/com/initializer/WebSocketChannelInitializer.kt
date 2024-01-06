package com.initializer

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.handler.timeout.IdleStateHandler
import java.net.URI
import java.util.concurrent.TimeUnit

typealias Pipeline = (channel: Channel, pipeline: ChannelPipeline) -> Unit

/**
 * websocket 通信
 */
@Sharable
class WebSocketChannelInitializer(
    /** 是否是服务器  */
    private val clientUri: URI?,
    private val pipeline: Pipeline?,
    private vararg val channelHandler: ChannelHandler
) : ChannelInitializer<Channel>() {

    constructor(vararg channelHandler: ChannelHandler) : this(null, null, *channelHandler)

    constructor(iPipeline: Pipeline, vararg channelHandler: ChannelHandler) : this(null, iPipeline, *channelHandler)

    /**
     *
     * @param clientUri 客户端使用的url
     * @param channelHandler
     */
    constructor(clientUri: URI, vararg channelHandler: ChannelHandler) : this(clientUri, null, *channelHandler)

    override fun initChannel(channel: Channel) {
        //此方法每次客户端连接都会调用，是为通道初始化的方法
        //获得通道channel中的管道链（执行链、handler链）
        val pipeline = channel.pipeline()
        // 添加一个http的编解码器
        if (clientUri == null) {
            pipeline.addLast("http-codec", HttpServerCodec())
        } else {
            pipeline.addLast("http-codec", HttpClientCodec())
        }
        // 添加一个用于支持大数据流的支持  分块传输
        pipeline.addLast("http-chunked", ChunkedWriteHandler())
        // 添加一个聚合器，这个聚合器主要是将HttpMessage聚合成FullHttpRequest/Response
        pipeline.addLast(HttpObjectAggregator(1024 * 64))
        // 需要指定接收请求的路由    必须使用以ws后缀结尾的url才能访问
        if (clientUri == null) {
            pipeline.addLast(
                WebSocketServerProtocolHandler(
                    "/ws",
                    null, false, Int.MAX_VALUE
                )
            )
        } else {
            pipeline.addLast(
                WebSocketClientProtocolHandler(
                    WebSocketClientHandshakerFactory.newHandshaker(
                        clientUri, WebSocketVersion.V13,
                        null, false, DefaultHttpHeaders(), Int.MAX_VALUE
                    )
                )
            )
        }

        //        pipeline.addLast(WebSocketDecoder.getInst(isEncrypt));// 使用单例 节约创建类
//        pipeline.addLast(WebSocketEncoder.getInst(isEncrypt));// 使用单例 节约创建类

        // 添加自定义的Handler
        for (i in channelHandler.indices) {
            val handler = channelHandler[i]
            if (handler is IdleStateHandler) {
                initIdle(pipeline, handler)
            } else {
                pipeline.addLast("handler$i", handler)
            }
        }
        this.pipeline?.invoke(channel, pipeline)
    }

    private fun initIdle(pipeline: ChannelPipeline, handler: IdleStateHandler) {
        pipeline.addFirst(
            IdleStateHandler(
                handler.readerIdleTimeInMillis,
                handler.writerIdleTimeInMillis,
                handler.allIdleTimeInMillis,
                TimeUnit.MILLISECONDS
            )
        )
    }

}



