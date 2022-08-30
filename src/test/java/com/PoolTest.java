package com;

import com.entity.GameOutput;
import com.socket.ActionData;
import com.util.PoolUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

public class PoolTest {

    @Before
    public void before() {
        System.setProperty("rootDir", "E:\\WorkSpace\\Idea\\Java\\NettySocket/webClient");
    }

    @SneakyThrows
    @Test
    public void test() {

        PoolUtils.DEFAULT_MAX_TOTAL = 1;
        PoolUtils.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = Duration.ofSeconds(6);
        PoolUtils.REMOVE_ABANDONED_TIMEOUT = Duration.ofSeconds(5);

        ActionData<?> a = PoolUtils.getObject(ActionData.class);
        System.out.println(a);
        System.out.println(PoolUtils.getNumIdle(ActionData.class));
        PoolUtils.returnObject(a);
        System.out.println(PoolUtils.getNumIdle(ActionData.class));
        a = PoolUtils.getObject(ActionData.class);
        System.out.println(a);

        StopWatch stopWatch = StopWatch.createStarted();
//        TimeUnit.SECONDS.sleep(6);
        a = PoolUtils.getObject(ActionData.class);
        System.out.println(a);
        System.out.println(stopWatch.getTime() / 1000);
        stopWatch.stop();

        GameOutput output = PoolUtils.getObject(GameOutput.class);
        System.out.println(output);
        System.out.println(PoolUtils.getNumIdle(GameOutput.class));
        PoolUtils.returnObject(output);
        System.out.println(PoolUtils.getNumIdle(GameOutput.class));
        output = PoolUtils.getObject(GameOutput.class);
        System.out.println(output);

    }

    @SneakyThrows
    @Test
    public void test2() {
        ActionData actionData = new ActionData();
        PoolUtils.getPool(ActionData.class).returnObject(actionData);
        PoolUtils.getPool(ActionData.class).returnObject(actionData);
        System.out.println(PoolUtils.getNumIdle(ActionData.class));
    }

    @SneakyThrows
    @Test
    public void test3() {
        ActionData<?> actionData = PoolUtils.getObject(ActionData.class);
        PoolUtils.getPool(ActionData.class).returnObject(actionData);
        PoolUtils.getPool(ActionData.class).returnObject(actionData);
        System.out.println(PoolUtils.getNumIdle(ActionData.class));
    }


}
