import com.entity.GameOutput;
import com.parse.MessageDecoder;
import com.parse.MessageEncoder;
import com.socket.ActionChannelAdapter;
import com.socket.ActionData;
import com.socket.ClientAcceptor;
import com.socket.SessionListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client implements SessionListener {

    public Client() {
        System.setProperty("rootDir", "E:\\WorkSpace\\Idea\\Java\\NettySocket");

        ClientAcceptor clientAcceptor = new ClientAcceptor(this);

        clientAcceptor.connect("0.0.0.0", 9099);

        try {
            //测试输入
            while (true) {
                System.out.println("客户端启动");
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
    public void sessionCreated(ChannelHandlerContext session) throws Exception {

    }

    @Override
    public void sessionClosed(ChannelHandlerContext session) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext session, Throwable cause) throws Exception {

    }

    @Override
    public void sessionIdle(ChannelHandlerContext session, IdleState status) {

    }

    @Override
    public void messageSent(ChannelHandlerContext session, Object message) throws Exception {

    }

    @Override
    public void messageReceived(ChannelHandlerContext session, Object message) {
        System.out.println("messageReceived:" + message);
    }

    @Override
    public void notRegAction(ChannelHandlerContext session, Object message) throws Exception {

    }

}
