package com.util;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

public class TimerUtils {

    public static HashedWheelTimer hashedWheelTimer;

    private static void init() {
        if (hashedWheelTimer == null) hashedWheelTimer = new HashedWheelTimer();
    }

    public static Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
        init();
        return hashedWheelTimer.newTimeout(task, delay, unit);
    }

    /**
     * 释放此计时器获取的所有资源，并取消所有已计划但尚未执行的任务。
     */
    public static void stop() {
        init();
        hashedWheelTimer.stop();
    }

}
