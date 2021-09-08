package com.ioByte;

import com.socket.ActionData;
import com.socket.ActionHandler;
import io.netty.channel.ChannelHandlerContext;

public class ByteChannelAdapter implements ActionHandler<Object> {


    @Override
    public void execute(ActionData<Object> actionData, ChannelHandlerContext session) {
        System.out.println(actionData.getAction());
        System.out.println(new String(actionData.getBuf()));
    }

}
