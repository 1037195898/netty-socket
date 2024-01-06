package com.initializer

import com.parse.MessageDecoder
import com.parse.MessageEncoder
import com.parse.MessageEncoder.Companion.inst
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.TimeUnit

/**
 * 二进制通信
 */
@Sharable
class ByteChannelHandler(private vararg val channelHandler: ChannelHandler) : ChannelInitializer<Channel>() {

    override fun initChannel(channel: Channel) {
        //此方法每次客户端连接都会调用，是为通道初始化的方法
        //获得通道channel中的管道链（执行链、handler链）
        val pipeline = channel.pipeline()
        pipeline.addLast(MessageDecoder()) // 解析不能使用共享  因为有粘包功能需要独立
        pipeline.addLast(inst) // 使用单例 节约创建类
        // 添加自定义的Handler
        for (i in channelHandler.indices) {
            val handler = channelHandler[i]
            if (handler is IdleStateHandler) {
                initIdle(pipeline, handler)
            } else {
                pipeline.addLast("handler$i", handler)
            }
        }
    }

    private fun initIdle(pipeline: ChannelPipeline, handler: IdleStateHandler) {
        pipeline.addLast(
            IdleStateHandler(
                handler.readerIdleTimeInMillis,
                handler.writerIdleTimeInMillis,
                handler.allIdleTimeInMillis,
                TimeUnit.MILLISECONDS
            )
        )
    }
}
