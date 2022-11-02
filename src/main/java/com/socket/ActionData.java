package com.socket;

import com.entity.GameInput;
import com.entity.GameOutput;
import com.interfaces.IPool;
import com.util.PoolUtils;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Duration;

@Accessors(chain = true)
@Data
public class ActionData<T> implements IPool<ActionData<T>> {

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

    public ActionData() {
    }

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

    @Override
    public ActionData<T> reset() {
        this.action = 0;
        this.verify = 0;
        this.sessionId = 0;
        this.buf = null;
        return this;
    }

    public static ActionData get() throws Exception {
        return PoolUtils.getObject(ActionData.class);
    }
    public static ActionData get(long borrowMaxWaitMillis) throws Exception {
        return PoolUtils.getObject(ActionData.class, borrowMaxWaitMillis);
    }
    public static ActionData get(Duration borrowMaxWaitDuration) throws Exception {
        return PoolUtils.getObject(ActionData.class, borrowMaxWaitDuration);
    }

    /** 生成数据并回收 */
    public void returnObject() {
        PoolUtils.returnObject(this);
    }

}
