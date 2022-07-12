package com.ioByte;

import com.socket.ActionData;
import com.socket.ActionHandler;
import com.socket.IoSession;

public class ByteChannelAdapter implements ActionHandler<Object> {


    @Override
    public void execute(ActionData<Object> actionData, IoSession session) {
        System.out.println(actionData.getAction());
        System.out.println(new String(actionData.getBuf()));
    }

}
