package com.parse;

import com.entity.GameInput;
import com.socket.ActionData;
import com.util.IOUtils;
import com.util.ZlibUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

@ChannelHandler.Sharable
public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * 接收最大字节  2M
     */
    public static int RECEIVE_MAX = 1024 * 1024 * 2;

    public MessageDecoder() {
        this(RECEIVE_MAX);
    }

    public MessageDecoder(int maxFrameLength) {
        super(maxFrameLength, 0, Integer.BYTES);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
//        return super.decode(ctx, in);
        if (in == null) {
            return null;
        }
        // 是否满足头的数据
        if (in.readableBytes() <= Integer.BYTES) {
            return null;
        }
        in.markReaderIndex(); // 标记
        int dataLength = in.readInt();
        // 可读数据不足
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex(); // 还原到标记处
            return null;
        }
        ByteBuf byteBuf = in.readBytes(dataLength);
        byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        // 解压
        byte[] by = ZlibUtil.decompress(bytes);
        // 解密
        by = IOUtils.getAes(ctx.channel()).decrypt(by);
        GameInput input = new GameInput(by);
        ActionData<?> data = new ActionData<>(0);
//        System.out.println("事件头="+data.getAction());
//        System.out.println("获取了事件头后剩余的="+input.available());
//        System.out.println("获取包头后的长度,"+input.available()+", "+buf.remaining());
        data.setVerify(input.readLong());
        data.setAction(input.readInt());
        int byteLen = input.readInt();// 获取长度
        bytes = new byte[byteLen];
        input.read(bytes, 0, byteLen);
        data.setBuf(bytes);
//        System.out.println("获取所有数据后的长度,"+input.available()+", "+buf.remaining());
//        System.out.println(data.getData());
        return data;
    }

}
