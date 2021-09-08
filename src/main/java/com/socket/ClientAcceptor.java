package com.socket;

import com.adapter.ActionChannelAdapter;
import com.initializer.ByteChannelHandler;
import com.util.ActionUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.URI;

public class ClientAcceptor {

    private final ActionChannelAdapter actionChannelAdapter;
    private Bootstrap bootstrap;
    private NioEventLoopGroup worker;
    private ChannelFuture channelFuture;

    public ClientAcceptor(SessionListener sessionListener) {
        this(sessionListener, null);
    }

    public ClientAcceptor(SessionListener sessionListener, ChannelHandler channelHandler) {
        init();
        actionChannelAdapter = new ActionChannelAdapter();
        ActionUtils.getInst().addSessionListener(sessionListener);
        //添加handler，管道中的处理器，通过ChannelInitializer来构造
        if (channelHandler == null) channelHandler = new ByteChannelHandler(actionChannelAdapter);
        bootstrap.handler(channelHandler);
    }

    public void connect(String host, int port) {
        //建立连接
        channelFuture = bootstrap.connect(host, port);
    }

    public void connect(URI uri) throws InterruptedException {
        //建立连接
        channelFuture = bootstrap.connect(uri.getHost(), uri.getPort());
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

}
