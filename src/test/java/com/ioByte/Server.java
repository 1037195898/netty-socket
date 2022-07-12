package com.ioByte;

import com.adapter.MessageAdapter;
import com.initializer.ByteChannelHandler;
import com.socket.ActionData;
import com.socket.IoSession;
import com.socket.ServerAcceptor;
import com.socket.SessionListener;
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
        ServerAcceptor serverAcceptor = new ServerAcceptor();
        serverAcceptor.addListener(this);
        serverAcceptor.handler(new ByteChannelHandler(
                new IdleStateHandler(5, 5, 10, TimeUnit.SECONDS),
                new MessageAdapter(serverAcceptor.getActionEventManager())));
        serverAcceptor.registerAction(new ByteChannelAdapter(), 100);
        serverAcceptor.bind(9099);
        System.out.println("测试服务器开启!按任意键+回车关闭");
    }

    public static void main(String[] args) {
        new Server();
    }

    @Override
    public void sessionCreated(IoSession session) {
        System.out.println("连接一个=" + session.channel().id().asLongText());
    }

    @Override
    public void sessionClosed(IoSession session) {
        System.out.println("断开一个"+session.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        session.channel().closeFuture();
        System.out.println("意外断开一个"+session.channel().id().asLongText());
//        cause.printStackTrace();
    }

    @Override
    public void sessionIdle(IoSession session, IdleState status) {
        System.out.println("sessionIdle");
    }

    @Override
    public void messageSent(Object message) {

    }

    @Override
    public void messageReceived(IoSession session, Object message) {
//        System.out.println("messageReceived");
    }

    @Override
    public void notRegAction(IoSession session, Object message) {
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
    public void handshakeComplete(IoSession session) {

    }

}