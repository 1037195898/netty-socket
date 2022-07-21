package com;

import com.socket.ActionData;
import com.socket.IoSession;
import com.util.ActionUtils;
import com.util.IOUtils;
import com.web.WebHandler;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class JavaTest {

    @Before
    public void before() {
        System.setProperty("rootDir", "E:\\WorkSpace\\Idea\\Java\\NettySocket/webClient");
    }

    @SneakyThrows
    @Test
    public void test() {

        WebHandler handler = new WebHandler();
        ActionUtils.addAction(handler);

        ActionUtils.run(1,
                new ActionData<>(1),
                new IoSession(null, null)
        );

    }


    @SneakyThrows
    @Test
    public void aesTest() {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                for (int j = 0; j < 2; j++) {
                    System.out.println(IOUtils.getAes());
                }
            }).start();
        }
        TimeUnit.SECONDS.sleep(5000);
    }

}
