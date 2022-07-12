package com.ioByte;

import com.adapter.MessageAdapter;
import com.initializer.ByteChannelHandler;
import com.socket.ActionData;
import com.socket.ClientAcceptor;
import com.socket.IoSession;
import com.socket.SessionListener;
import io.netty.handler.timeout.IdleState;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client implements SessionListener {

    public Client() {
        System.setProperty("rootDir", "E:\\WorkSpace\\Idea\\Java\\NettySocket");

        ClientAcceptor clientAcceptor = new ClientAcceptor();
        clientAcceptor.addListener(this);
        clientAcceptor.handler(new ByteChannelHandler(
                new MessageAdapter(clientAcceptor.getActionEventManager())));
        clientAcceptor.registerAction(new ByteChannelAdapter(), 100);
        clientAcceptor.connect("0.0.0.0", 9099);

        try {
            System.out.println("客户端启动");
            //测试输入
            while (true) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("请输入：");
                String msg = scanner.nextLine();
                ActionData<?> action = new ActionData<>(100);
                action.setBuf(msg.getBytes(StandardCharsets.UTF_8));
                clientAcceptor.writeAndFlush(action);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public static void main(String[] args) {
        new Client();
    }

    @Override
    public void sessionCreated(IoSession session) {

    }

    @Override
    public void sessionClosed(IoSession session) {

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {

    }

    @Override
    public void sessionIdle(IoSession session, IdleState status) {

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

    }

    @Override
    public void handshakeComplete(IoSession session) {

    }

}
