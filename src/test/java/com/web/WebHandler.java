package com.web;

import com.annotation.SocketAction;
import com.entity.GameInput;
import com.socket.ActionData;
import com.socket.ActionHandler;
import com.socket.IoSession;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WebHandler implements ActionHandler<Object> {

    @Override
    public void execute(ActionData<Object> actionData, IoSession session) {
        GameInput gameInput = new GameInput(actionData.getBuf());
        try {
            LoggerFactory.getLogger(getClass()).debug("channelRead0: " + gameInput.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SocketAction(1)
    private void heartbeat() {
        System.out.println("心跳1");
    }

    @SocketAction(1)
    public void heartbeat(IoSession session) {
        System.out.println("心跳2 " + session);
    }

    @SocketAction(1)
    public void heartbeat(ActionData<Object> actionData, IoSession session) {
        System.out.println("心跳3 " + actionData + " " + session);
    }

    @SocketAction(1)
    private void heartbeat(ActionData<Object> actionData) {
        System.out.println("心跳4 " + actionData);
    }

}