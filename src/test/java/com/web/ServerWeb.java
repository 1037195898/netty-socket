package com.web;

import com.adapter.MessageAdapter;
import com.initializer.WebSocketChannelInitializer;
import com.parse.WebSocketDecoder;
import com.parse.WebSocketEncoder;
import com.socket.ActionData;
import com.socket.IoSession;
import com.socket.ServerAcceptor;
import com.socket.SessionListener;
import com.util.SocketType;
import com.util.SocketUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public class ServerWeb implements SessionListener {

    public ServerWeb() {
        LoggerFactory.getLogger(getClass()).info("开始");

        SocketUtils.webSocketType = SocketType.BINARY_WEB_SOCKET_FRAME;
        ServerAcceptor serverAcceptor = new ServerAcceptor();
        serverAcceptor.addListener(this);
        serverAcceptor.handler(new WebSocketChannelInitializer(
                new MessageAdapter(serverAcceptor.getActionEventManager()),
                new IdleStateHandler(5, 5, 10, TimeUnit.SECONDS),
                WebSocketDecoder.getInst(true),
                WebSocketEncoder.getInst(true)
        ));
        serverAcceptor.registerAction(new WebHandler(), 100, 1);
        serverAcceptor.bind(9099);
        System.out.println("测试服务器开启!按任意键+回车关闭");
    }

    public static void main(String[] args) {
        System.setProperty("rootDir", "E:\\WorkSpace\\Idea\\Java\\NettySocket/webServer");
        new ServerWeb();
    }

    @Override
    public void sessionCreated(IoSession session) {
        System.out.println("连接一个=" + session.channel().id() + " ," + session.channel().id().asLongText());
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
    public void messageSent(IoSession session, Object message) {

    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        System.out.println("messageReceived:" + message);
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