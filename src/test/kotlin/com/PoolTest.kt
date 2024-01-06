package com

import com.entity.Output
import com.socket.ActionData
import com.util.PoolUtils
import org.junit.Before
import org.junit.Test
import kotlin.system.measureTimeMillis

class PoolTest {
    @Before
    fun before() {
        System.setProperty("rootDir", "D:\\WorkSpace\\Idea\\Java\\NettySocket/webClient")
    }

    @Test
    fun test() {
        PoolUtils.DEFAULT_MAX_TOTAL = 1
//        PoolUtils.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = java.time.Duration.ofSeconds(6)
        PoolUtils.REMOVE_ABANDONED_TIMEOUT = java.time.Duration.ofSeconds(3)
        var poll = PoolUtils.getObject(ActionData::class.java)
        println("获取 $poll")
        println("当前池中数量：${PoolUtils.getNumIdle(ActionData::class.java)}")

        PoolUtils.returnObject(poll)
        println("回收后 池中：${PoolUtils.getNumIdle(ActionData::class.java)}")

        println("----------- 测试池中没有了  自动回收在获取 ------------")
        poll = PoolUtils.getObject(ActionData::class.java).also { it.action = 100 }
        println("获取一次 $poll")
        println("池中：${PoolUtils.getNumIdle(ActionData::class.java)}")
        var poll2: ActionData<*>
        val time = measureTimeMillis {
            poll2 = PoolUtils.getObject(ActionData::class.java)
        }
        println("获取 $poll2")
        println("执行经过 ${time / 1000}s")

        println("one: $poll   two: $poll2")
        poll.returnObject()
        poll2.returnObject()
        println("池中：${PoolUtils.getNumIdle(ActionData::class.java)}")


        println(" --------- output --------")
        var output = PoolUtils.getObject(Output::class.java)
        println("获取 $output")
        println("当前池中数量：${PoolUtils.getNumIdle(Output::class.java)}")
        PoolUtils.returnObject(output)
        println("回收后 池中：${PoolUtils.getNumIdle(Output::class.java)}")
        output = PoolUtils.getObject(Output::class.java)
        println("获取 $output")
    }

    @Test
    fun test2() {
        val actionData = ActionData<Any>()
        PoolUtils.getPool(ActionData::class.java).returnObject(actionData)
        PoolUtils.getPool(ActionData::class.java).returnObject(actionData)
        println(PoolUtils.getNumIdle(ActionData::class.java))
    }

    @Test
    fun test3() {
        val actionData = PoolUtils.getObject(ActionData::class.java)
        PoolUtils.getPool(ActionData::class.java).returnObject(actionData)
        PoolUtils.getPool(ActionData::class.java).returnObject(actionData)
        println(PoolUtils.getNumIdle(ActionData::class.java))
    }
}
