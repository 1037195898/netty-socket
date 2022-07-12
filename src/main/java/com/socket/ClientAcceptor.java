package com.socket;

import com.util.IOUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import lombok.Getter;

import java.net.URI;

@Getter
public class ClientAcceptor {

    private Bootstrap bootstrap;
    private NioEventLoopGroup worker;
    private ChannelFuture channelFuture;

    ActionEventManager actionEventManager;

    public ClientAcceptor() {
        init();
    }

    public void addListener(SessionListener sessionListener) {
        actionEventManager.addSessionListener(sessionListener);
    }

    public void handler(ChannelHandler channelHandler) {
        //添加handler，管道中的处理器，通过ChannelInitializer来构造
        bootstrap.handler(channelHandler);
    }

    public ChannelFuture connect(String host, int port) {
        //建立连接
        channelFuture = bootstrap.connect(host, port);
        return channelFuture;
    }

    public ChannelFuture connect(URI uri) {
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

    /**
     * 这次发送只能是最后一次连接的socket 发送消息并获得成功监听
     *
     * @param msg
     * @return
     */
    public ChannelFuture writeFlush(Object msg) {
        return IOUtils.getSession(channelFuture.channel()).writeFlush(msg);
    }

    /**
     * 这次发送只能是最后一次连接的socket 直接发送消息不监听成功与否
     * @param msg
     * @return
     */
    public ChannelFuture writeAndFlush(Object msg) {
        return IOUtils.getSession(channelFuture.channel()).writeAndFlush(msg);
    }

    public ChannelFuture write(Object msg) {
        return IOUtils.getSession(channelFuture.channel()).write(msg);
    }

    public Channel flush() {
        return IOUtils.getSession(channelFuture.channel()).flush();
    }

    public Future<?> stop() {
        //关闭连接
        return worker.shutdownGracefully();
    }

    private void init() {
        actionEventManager = new ActionEventManager();
        //定义服务类
        bootstrap = new Bootstrap();
        //定义执行线程组
        worker = new NioEventLoopGroup();
        //设置线程池
        bootstrap.group(worker);
        //设置通道
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);//加上这句话，避免重启时提示地址被占用
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
