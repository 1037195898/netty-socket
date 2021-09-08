package com.web;

import com.entity.GameInput;
import com.socket.ActionData;
import com.socket.ActionHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WebHandler implements ActionHandler<Object> {

    @Override
    public void execute(ActionData<Object> actionData, ChannelHandlerContext session) {
        GameInput gameInput = new GameInput(actionData.getBuf());
        try {
            LoggerFactory.getLogger(getClass()).debug("channelRead0: " + gameInput.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}