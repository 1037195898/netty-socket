package com.web;

import com.adapter.MessageAdapter;
import com.entity.GameOutput;
import com.initializer.WebSocketChannelInitializer;
import com.socket.ActionData;
import com.socket.ClientAcceptor;
import com.socket.SessionListener;
import com.util.SocketType;
import com.util.SocketUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ClientWeb implements SessionListener {

    public ClientWeb() {
        System.setProperty("rootDir", "E:\\WorkSpace\\Idea\\Java\\NettySocket");
        URI uri = URI.create("ws://localhost:9099/ws");

        SocketUtils.webSocketType = SocketType.BINARY_WEB_SOCKET_FRAME;
        ClientAcceptor clientAcceptor = new ClientAcceptor();
        clientAcceptor.addListener(this);
        clientAcceptor.handler(new WebSocketChannelInitializer(uri, false,
                (channel, pipeline) -> {
                    try {
                        int port = uri.getPort();
                        if (port == -1) {
                            if (uri.getScheme().equals("wss")) {
                                port = 443;
                            } else {
                                port = 80;
                            }
                        }
                        SslContext t = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                        SslHandler c = t.newHandler(channel.alloc(), uri.getHost(), port);
                        pipeline.addFirst(c);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                new MessageAdapter(clientAcceptor.getActionEventManager()),
                new IdleStateHandler(5, 5, 10, TimeUnit.SECONDS)
        ));
        clientAcceptor.registerAction(new WebHandler(), 100);
        try {
            clientAcceptor.connect(uri);
            System.out.println("客户端启动");
            //测试输入
            while (true) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("请输入：");
                String msg = scanner.nextLine();
                GameOutput gameOutput = new GameOutput();
                gameOutput.writeUTF(msg);
                ActionData<?> action = new ActionData<>(100);
                action.setBuf(gameOutput.toByteArray());
                clientAcceptor.writeAndFlush(action);
//                clientAcceptor.writeAndFlush(new TextWebSocketFrame(msg));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public static void main(String[] args) {
        new ClientWeb();
    }

    @Override
    public void sessionCreated(ChannelHandlerContext session) {

    }

    @Override
    public void sessionClosed(ChannelHandlerContext session) {
        System.out.println("断开了");
        System.exit(0);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext session, Throwable cause) {

    }

    @Override
    public void sessionIdle(ChannelHandlerContext session, IdleState status) {

    }

    @Override
    public void messageSent(Object message) {

    }

    @Override
    public void messageReceived(ChannelHandlerContext session, Object message) {
        System.out.println("messageReceived:" + message);
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

