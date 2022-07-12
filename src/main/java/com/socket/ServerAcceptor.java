package com.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import org.slf4j.LoggerFactory;

@Getter
public class ServerAcceptor {

    ServerBootstrap serverBootstrap;
    EventLoopGroup boss;
    EventLoopGroup worker;
    ActionEventManager actionEventManager;

    public ServerAcceptor() {
        init();
    }

    private void init() {
        actionEventManager = new ActionEventManager();
        //定义server启动类
        serverBootstrap = new ServerBootstrap();
        //定义工作组:boss分发请求给各个worker:boss负责监听端口请求，worker负责处理请求（读写）
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        //定义工作组
        serverBootstrap.group(boss, worker);
        //设置通道channel
        serverBootstrap.channel(NioServerSocketChannel.class);//A
        //设置参数，TCP参数
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 2048);   //连接缓冲池的大小
        serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, true);//加上这句话，避免重启时提示地址被占用
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);//维持链接的活跃，清除死链接
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);//关闭延迟发送
    }

    public void addListener(SessionListener sessionListener) {
        actionEventManager.addSessionListener(sessionListener);
    }

    public void handler(ChannelHandler channelHandler) {
        //添加handler，管道中的处理器，通过ChannelInitializer来构造
        serverBootstrap.childHandler(channelHandler);
    }

    /**
     * 绑定一个端口
     * @param port 端口
     * @return
     */
    public ChannelFuture bind(int port) {
        try {
            //绑定ip和port
            ChannelFuture channelFuture = serverBootstrap.bind("0.0.0.0", port).sync();//Future模式的channel对象
            LoggerFactory.getLogger(getClass()).info("服务器启动成功!");
            return channelFuture;
        } catch (InterruptedException e) {
            LoggerFactory.getLogger(getClass()).error("server start got exception!", e);
        }
        return null;
    }

    /**
     * 绑定一个端口 会调用channelFuture.channel().closeFuture().sync()  阻止主线程关闭
     * @param port 端口
     */
    public void bindSync(int port) {
        try {
            ChannelFuture channelFuture = bind(port);//Future模式的channel对象
            //监听关闭
            channelFuture.channel().closeFuture().sync();  //等待服务关闭，关闭后应该释放资源
        } catch (InterruptedException e) {
            LoggerFactory.getLogger(getClass()).error("server start got exception!", e);
        }
    }

    public void stop() {
        // 优雅的关闭资源
        boss.shutdownGracefully();
        worker.shutdownGracefully();
    }

    /**
     * 注册动作
     *
     * @param handler 处理器
     * @param actions 动作
     */
    public void registerAction(ActionHandler<?> handler, int... actions) {
        for (int action : actions) {
            actionEventManager.registerAction(action, handler);
        }
    }

    /**
     * 删除动作
     *
     * @param actions 动作
     */
    public void removeAction(int... actions) {
        for (int action : actions) {
            actionEventManager.removeAction(action);
        }
    }

    /**
     * 获取动作
     *
     * @param action 动作
     * @return
     */
    public ActionHandler<?> getAction(int action) {
        return actionEventManager.getAction(action);
    }

}
