package com.socket;

import com.entity.GameInput;
import com.entity.GameOutput;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
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

    public GameInput getInput() {
        return new GameInput(buf);
    }

}
