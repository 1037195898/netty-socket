package com.web;

import com.entity.GameInput;
import com.socket.ActionData;
import com.socket.ActionHandler;
import com.socket.IoSession;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WebHandler implements ActionHandler<Object> {

    @Override
    public void execute(ActionData<Object> actionData, IoSession session) {

        if (actionData.getAction() == 1) {
            System.out.println("心跳");
            return;
        }

        GameInput gameInput = new GameInput(actionData.getBuf());
        try {
            LoggerFactory.getLogger(getClass()).debug("channelRead0: " + gameInput.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}