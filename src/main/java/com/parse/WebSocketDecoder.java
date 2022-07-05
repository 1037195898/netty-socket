package com.parse;

import com.entity.GameInput;
import com.socket.ActionData;
import com.util.IOUtils;
import com.util.SocketType;
import com.util.SocketUtils;
import com.util.ZlibUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 解析收到的数据
 */
@ChannelHandler.Sharable
public class WebSocketDecoder extends ChannelInboundHandlerAdapter {

    /**
     * 是否加密
     */
    private boolean isEncrypt;
    private static volatile WebSocketDecoder encoder;

    public static WebSocketDecoder getInst() {
        if (encoder == null) {
            synchronized (WebSocketDecoder.class) {
                if (encoder == null) {
                    encoder = new WebSocketDecoder();
                }
            }
        }
        return encoder;
    }

    public static WebSocketDecoder getInst(boolean isEncrypt) {
        if (encoder == null) {
            synchronized (WebSocketDecoder.class) {
                if (encoder == null) {
                    encoder = new WebSocketDecoder();
                }
            }
        }
        encoder.isEncrypt = isEncrypt;
        return encoder;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LoggerFactory.getLogger(getClass()).debug("WebSocketDecoder.channelRead : " + msg);
        channelReadT(ctx, msg);
    }

    private void channelReadT(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = null;
        if (msg instanceof TextWebSocketFrame) {
            byteBuf = ((TextWebSocketFrame) msg).content();
//            String content = ((TextWebSocketFrame) msg).text();
//            ActionData<?> data = new ActionData<>(-100);
//            data.setBuf(content.getBytes(StandardCharsets.UTF_8));
//            ctx.fireChannelRead(data);
        } else if (msg instanceof BinaryWebSocketFrame) {
            byteBuf = Unpooled.copiedBuffer(((BinaryWebSocketFrame) msg).content());
        }
        if (byteBuf == null) {
            ctx.fireChannelRead(msg);
            return;
        }
        byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        if (isEncrypt) {
            // 解压
            bytes = ZlibUtil.decompress(bytes);
            // 解密
            bytes = IOUtils.getAes(ctx.channel()).decrypt(bytes);
        }
        ActionData<?> data = new ActionData<>(0);
//        System.out.println("事件头="+data.getAction());
//        System.out.println("获取了事件头后剩余的="+input.available());
//        System.out.println("获取包头后的长度,"+input.available()+", "+buf.remaining());
        if (SocketUtils.webSocketType == SocketType.TEXT_WEB_SOCKET_FRAME) {
            data.setVerify(System.currentTimeMillis());
            data.setBuf(bytes);
        } else {
            GameInput input = new GameInput(bytes);
            data.setVerify(input.readLong());
            data.setAction(input.readInt());
            int byteLen = input.readInt();// 获取长度
            bytes = new byte[byteLen];
            input.read(bytes, 0, byteLen);
            data.setBuf(bytes);
        }
//        System.out.println("获取所有数据后的长度,"+input.available()+", "+buf.remaining());
//        System.out.println(data.getData());
        ctx.fireChannelRead(data);
        ReferenceCountUtil.release(msg);
    }

}
