package com.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
        actionChannelAdapter.addSessionListener(sessionListener);
        //添加handler，管道中的处理器，通过ChannelInitializer来构造
        if (channelHandler == null) channelHandler = new ByteChannelHandler(actionChannelAdapter);
        bootstrap.handler(channelHandler);


    }

    public void connect(String host, int port) {
        //建立连接
        channelFuture = bootstrap.connect(host,port);
//        try {
//            //测试输入
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
//            while(true){
//                System.out.println("请输入：");
//                String msg = bufferedReader.readLine();
//                ActionData<?> action = new ActionData<>(100);
//                action.setBuf(msg.getBytes(StandardCharsets.UTF_8));
//                channelFuture.channel().writeAndFlush(action);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }finally {
//
//        }
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
