package com;

import com.util.TimerUtils;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class NettyTest {

    @Before
    public void before() {
        System.setProperty("rootDir", "E:\\WorkSpace\\Idea\\Java\\NettySocket/webClient");
    }

    @SneakyThrows
    @Test
    public void HashedWheelTimerTest() {
        System.out.println(Thread.currentThread().getName());

        TimerUtils.newTimeout(task, 1, TimeUnit.SECONDS);

        TimeUnit.SECONDS.sleep(20);
    }

    private TimerTask task = new TimerTask() {
        @Override
        public void run(Timeout timeout) throws Exception {
            System.out.println("aa | " + Thread.currentThread().getName());
            TimerUtils.newTimeout(task, 1, TimeUnit.SECONDS);
        }
    };



}
