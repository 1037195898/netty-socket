package com.socket

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.LoggerFactory

class ServerAcceptor {
    lateinit var serverBootstrap: ServerBootstrap
    lateinit var boss: EventLoopGroup
    lateinit var worker: EventLoopGroup
    lateinit var actionEventManager: ActionEventManager

    init {
        init()
    }

    private fun init() {
        actionEventManager = ActionEventManager()
        //定义server启动类
        serverBootstrap = ServerBootstrap()
        //定义工作组:boss分发请求给各个worker:boss负责监听端口请求，worker负责处理请求（读写）
        boss = NioEventLoopGroup()
        worker = NioEventLoopGroup()
        //定义工作组
        serverBootstrap.group(boss, worker)
        //设置通道channel
        serverBootstrap.channel(NioServerSocketChannel::class.java) //A
        //设置参数，TCP参数
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 2048) //连接缓冲池的大小
        serverBootstrap.option(ChannelOption.SO_REUSEADDR, true) //加上这句话，避免重启时提示地址被占用
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true) //维持链接的活跃，清除死链接
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true) //关闭延迟发送
    }

    fun <T : Any> addListener(sessionListener: SessionListener<T>) {
        actionEventManager.addSessionListener(sessionListener)
    }

    fun handler(channelHandler: ChannelHandler?) {
        //添加handler，管道中的处理器，通过ChannelInitializer来构造
        serverBootstrap.childHandler(channelHandler)
    }

    /**
     * 绑定一个端口
     * @param port 端口
     * @return
     */
    fun bind(port: Int): ChannelFuture? {
        try {
            //绑定ip和port
            val channelFuture = serverBootstrap.bind("0.0.0.0", port).sync() //Future模式的channel对象
            LoggerFactory.getLogger(javaClass).info("服务器启动成功!")
            return channelFuture
        } catch (e: InterruptedException) {
            LoggerFactory.getLogger(javaClass).error("server start got exception!", e)
        }
        return null
    }

    /**
     * 绑定一个端口 会调用channelFuture.channel().closeFuture().sync()  阻止主线程关闭
     * @param port 端口
     */
    fun bindSync(port: Int) {
        try {
            val channelFuture = bind(port) //Future模式的channel对象
            //监听关闭
            channelFuture!!.channel().closeFuture().sync() //等待服务关闭，关闭后应该释放资源
        } catch (e: InterruptedException) {
            LoggerFactory.getLogger(javaClass).error("server start got exception!", e)
        }
    }

    /**
     * 优雅的关闭资源
     */
    fun stop() {
        boss.shutdownGracefully()
        worker.shutdownGracefully()
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
