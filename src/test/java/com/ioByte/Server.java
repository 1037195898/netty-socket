package com.ioByte;

import com.adapter.MessageAdapter;
import com.initializer.ByteChannelHandler;
import com.parse.MessageDecoder;
import com.parse.MessageEncoder;
import com.socket.ActionData;
import com.socket.ActionHandler;
import com.socket.ServerAcceptor;
import com.socket.SessionListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public class Server implements SessionListener {

    public Server() {
        System.setProperty("rootDir", "E:\\WorkSpace\\Idea\\Java\\NettySocket");
        LoggerFactory.getLogger(getClass()).info("开始");
        ServerAcceptor serverAcceptor = new ServerAcceptor(this,
                new ByteChannelHandler(
                        new IdleStateHandler(5, 5, 10, TimeUnit.SECONDS),
                        new MessageAdapter()));
        serverAcceptor.registerAction(new ByteChannelAdapter(), 100);
        serverAcceptor.bind(9099);
        System.out.println("测试服务器开启!按任意键+回车关闭");
    }

    public static void main(String[] args) {
        new Server();
    }

    @Override
    public void sessionCreated(ChannelHandlerContext session) {
        System.out.println("连接一个=" + session.channel().id().asLongText());
    }

    @Override
    public void sessionClosed(ChannelHandlerContext session) {
        System.out.println("断开一个"+session.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext session, Throwable cause) {
        session.channel().closeFuture();
        System.out.println("意外断开一个"+session.channel().id().asLongText());
//        cause.printStackTrace();
    }

    @Override
    public void sessionIdle(ChannelHandlerContext session, IdleState status) {
        System.out.println("sessionIdle");
    }

    @Override
    public void messageSent(Object message) {

    }

    @Override
    public void messageReceived(ChannelHandlerContext session, Object message) {
//        System.out.println("messageReceived");
    }

    @Override
    public void notRegAction(ChannelHandlerContext session, Object message) {
        if (message instanceof ActionData<?>) {
            if (((ActionData<?>) message).getAction() == -100) {
                LoggerFactory.getLogger(getClass()).debug("notRegAction:" +
                        StringUtils.toEncodedString(((ActionData<?>) message).getBuf(), Charset.defaultCharset()));
            } else {
                LoggerFactory.getLogger(getClass()).debug("notRegAction:" + message);
            }
        }
    }

    @Override
    public void handshakeComplete(ChannelHandlerContext session) {

    }

}