package com.socket;

import com.entity.GameInput;
import com.entity.GameOutput;

public class ActionData<T> {

    /**
     * 动作
     */
    private int action;
    /**
     * 用于IOByte通信
     */
    private byte[] buf;
    /**
     * 验证信息
     */
    private long verify;
    /**
     * 如果存在  那么在发送的时候  会发送此id
     */
    private long sessionId;

    public ActionData(int action) {
        this.action = action;
    }

    public ActionData(int action, long verify) {
        super();
        this.action = action;
        this.verify = verify;
    }

    public ActionData(int action, byte[] buf) {
        super();
        this.action = action;
        this.buf = buf;
    }

    public ActionData(int action, GameOutput output) {
        super();
        this.action = action;
        this.buf = output.toByteArray();
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public long getVerify() {
        return verify;
    }

    public void setVerify(long verify) {
        this.verify = verify;
    }

    public byte[] getBuf() {
        return buf;
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    public GameInput getInput() {
        return new GameInput(buf);
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "ActionData [action=" + action + ", buf=" + (buf == null ? buf : buf.length)
                + ", verify=" + verify + ", sessionId=" + sessionId + "]";
    }

}
