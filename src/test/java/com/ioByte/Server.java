package com.ioByte;

import com.socket.ActionData;
import com.socket.ActionHandler;
import com.socket.ServerAcceptor;
import com.socket.SessionListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import org.slf4j.LoggerFactory;

public class Server implements SessionListener {

    public Server() {
        System.setProperty("rootDir", "E:\\WorkSpace\\Idea\\Java\\NettySocket");
        LoggerFactory.getLogger(getClass()).info("开始");
        ServerAcceptor serverAcceptor = new ServerAcceptor(this);
        serverAcceptor.registerAction(new TestHandler(), 100);
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
        cause.printStackTrace();
    }

    @Override
    public void sessionIdle(ChannelHandlerContext session, IdleState status) {
        System.out.println("sessionIdle");
    }

    @Override
    public void messageSent(ChannelHandlerContext session, Object message) {

    }

    @Override
    public void messageReceived(ChannelHandlerContext session, Object message) {
//        System.out.println("messageReceived");
    }

    @Override
    public void notRegAction(ChannelHandlerContext session, Object message) {

    }

}

class TestHandler implements ActionHandler<Object> {

    @Override
    public void execute(ActionData<Object> actionData, ChannelHandlerContext session) {

        System.out.println(actionData.getAction());
        System.out.println(new String(actionData.getBuf()));

    }

}