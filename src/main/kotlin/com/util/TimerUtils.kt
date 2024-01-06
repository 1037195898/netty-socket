package com.util

import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import java.util.concurrent.TimeUnit

object TimerUtils {
    var hashedWheelTimer: HashedWheelTimer? = null

    private fun init() {
        if (hashedWheelTimer == null) hashedWheelTimer = HashedWheelTimer()
    }

    @JvmStatic
    fun newTimeout(task: TimerTask?, delay: Long, unit: TimeUnit?): Timeout {
        init()
        return hashedWheelTimer!!.newTimeout(task, delay, unit)
    }

    /**
     * 释放此计时器获取的所有资源，并取消所有已计划但尚未执行的任务。
     */
    fun stop() {
        init()
        hashedWheelTimer!!.stop()
    }
}
