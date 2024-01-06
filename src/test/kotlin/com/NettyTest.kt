package com

import com.util.TimerUtils
import io.netty.util.Timeout
import io.netty.util.TimerTask
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class NettyTest {

    @Before
    fun before() {
        System.setProperty("rootDir", "D:\\WorkSpace\\Idea\\Java\\NettySocket/webClient")
    }

    @Test
    fun HashedWheelTimerTest() {
        println(Thread.currentThread().name)

        TimerUtils.newTimeout(task, 1, TimeUnit.SECONDS)

        TimeUnit.SECONDS.sleep(20)
    }

    private val task = object : TimerTask {

        override fun run(timeout: Timeout?) {
            println("aa | " + Thread.currentThread().name)
            TimerUtils.newTimeout(this, 1, TimeUnit.SECONDS)
        }
    }
}
