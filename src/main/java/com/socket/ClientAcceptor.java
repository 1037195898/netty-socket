package com.socket;

import com.util.ActionUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.URI;

public class ClientAcceptor {

    private Bootstrap bootstrap;
    private NioEventLoopGroup worker;
    private ChannelFuture channelFuture;

    public ClientAcceptor(SessionListener sessionListener, ChannelHandler channelHandler) {
        init();
        ActionUtils.getInst().addSessionListener(sessionListener);
        //添加handler，管道中的处理器，通过ChannelInitializer来构造
        bootstrap.handler(channelHandler);
    }

    public ChannelFuture connect(String host, int port) {
        //建立连接
        channelFuture = bootstrap.connect(host, port);
        return channelFuture;
    }

    public ChannelFuture connect(URI uri) throws InterruptedException {
        //建立连接
        int port = uri.getPort();
        if (port == -1) {
            if (uri.getScheme().equals("wss")) {
                port = 443;
            } else {
                port = 80;
            }
        }
        return connect(uri.getHost(), port);
    }

    public void writeAndFlush(Object msg) {
        channelFuture.channel().writeAndFlush(msg);
    }

    public void stop() {
        //关闭连接
        worker.shutdownGracefully();
    }

    private void init() {
        //定义服务类
        bootstrap = new Bootstrap();
        //定义执行线程组
        worker = new NioEventLoopGroup();
        //设置线程池
        bootstrap.group(worker);
        //设置通道
        bootstrap.channel(NioSocketChannel.class);
    }

    /**
     * 注册动作
     *
     * @param handler 处理器
     * @param actions 动作
     */
    public void registerAction(ActionHandler<?> handler, int... actions) {
        for (int action : actions) {
            ActionUtils.getInst().registerAction(action, handler);
        }
    }

    /**
     * 删除动作
     *
     * @param actions 动作
     */
    public void removeAction(int... actions) {
        for (int action : actions) {
            ActionUtils.getInst().removeAction(action);
        }
    }

    /**
     * 获取动作
     *
     * @param action 动作
     * @return
     */
    public ActionHandler<?> getAction(int action) {
        return ActionUtils.getInst().getAction(action);
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

}
