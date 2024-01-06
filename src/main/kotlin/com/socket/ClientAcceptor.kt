package com.socket

import com.util.IOUtils
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.concurrent.Future
import java.net.URI

class ClientAcceptor {

    lateinit var bootstrap: Bootstrap
    lateinit var worker: NioEventLoopGroup
    var channelFuture: ChannelFuture? = null
    lateinit var actionEventManager: ActionEventManager

    init {
        init()
    }

    private fun init() {
        actionEventManager = ActionEventManager()
        //定义服务类
        bootstrap = Bootstrap()
        //定义执行线程组
        worker = NioEventLoopGroup()
        //设置线程池
        bootstrap.group(worker)
        //设置通道
        bootstrap.channel(NioSocketChannel::class.java)
        bootstrap.option(ChannelOption.SO_REUSEADDR, true) //加上这句话，避免重启时提示地址被占用
    }

    fun <T> addListener(sessionListener: SessionListener<T>) {
        actionEventManager.addSessionListener(sessionListener)
    }

    fun handler(channelHandler: ChannelHandler) {
        //添加handler，管道中的处理器，通过ChannelInitializer来构造
        bootstrap.handler(channelHandler)
    }

    fun connect(host: String, port: Int): ChannelFuture? {
        //建立连接
        return bootstrap.connect(host, port).also { channelFuture = it }
    }

    fun connect(uri: URI): ChannelFuture? {
        //建立连接
        var port = uri.port
        if (port == -1) {
            port = if (uri.scheme == "wss") {
                443
            } else {
                80
            }
        }
        return connect(uri.host, port)
    }

    /**
     * 这次发送只能是最后一次连接的socket 发送消息并获得成功监听
     *
     * @param msg
     * @return
     */
    fun writeFlush(msg: Any): ChannelFuture? {
        return IOUtils.getSession(channelFuture!!.channel())?.writeFlush(msg)
    }

    /**
     * 这次发送只能是最后一次连接的socket 直接发送消息不监听成功与否
     * @param msg
     * @return
     */
    fun writeAndFlush(msg: Any?): ChannelFuture? {
        return IOUtils.getSession(channelFuture!!.channel())?.writeAndFlush(msg)
    }

    fun write(msg: Any?): ChannelFuture? {
        return IOUtils.getSession(channelFuture!!.channel())?.write(msg)
    }

    fun flush(): Channel? {
        return IOUtils.getSession(channelFuture!!.channel())?.flush()
    }

    fun stop(): Future<*> {
        //关闭连接
        return worker.shutdownGracefully()
    }

    /**
     * 注册动作
     *
     * @param handler 处理器
     * @param actions 动作
     */
    fun registerAction(handler: ActionHandler<Any>, vararg actions: Int) {
        for (action in actions) {
            actionEventManager.registerAction(action, handler)
        }
    }

    /**
     * 删除动作
     *
     * @param actions 动作
     */
    fun removeAction(vararg actions: Int) {
        for (action in actions) {
            actionEventManager.removeAction(action)
        }
    }

    /**
     * 获取动作
     *
     * @param action 动作
     * @return
     */
    fun getAction(action: Int): ActionHandler<Any>? {
        return actionEventManager.getAction(action)
    }
}
