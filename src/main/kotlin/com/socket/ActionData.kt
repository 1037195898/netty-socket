package com.socket

import com.entity.Input
import com.entity.Output
import com.interfaces.IPool
import com.util.PoolUtils
import java.time.Duration

data class ActionData<T>(
    /**
     * 动作
     */
    var action: Int = 0,
    /**
     * 用于IOByte通信
     */
    var buf: ByteArray? = null,
    /**
     * 验证信息
     */
    var verify: Long = 0,

    /**
     * 如果存在  那么在发送的时候  会发送此id
     */
    var sessionId: Long = 0,

    ) : IPool<ActionData<T>> {

    constructor(action: Int, output: Output) : this() {
        this.action = action
        this.buf = output.toByteArray()
    }

    val input: Input
        get() = Input(buf!!)

    override fun reset(): ActionData<T> {
        this.action = 0
        this.verify = 0
        this.sessionId = 0
        this.buf = null
        return this
    }

    /** 生成数据并回收  */
    fun returnObject() {
        PoolUtils.returnObject<ActionData<*>>(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActionData<*>

        if (action != other.action) return false
        if (verify != other.verify) return false
        if (sessionId != other.sessionId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = action
        result = 31 * result + verify.hashCode()
        result = 31 * result + sessionId.hashCode()
        return result
    }

    companion object {
        fun get(): ActionData<*> {
            return PoolUtils.getObject(ActionData::class.java)
        }

        fun get(borrowMaxWaitMillis: Long): ActionData<*> {
            return PoolUtils.getObject(ActionData::class.java, borrowMaxWaitMillis)
        }

        fun get(borrowMaxWaitDuration: Duration): ActionData<*> {
            return PoolUtils.getObject(ActionData::class.java, borrowMaxWaitDuration)
        }
    }
}
