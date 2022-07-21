package com;

import com.socket.ActionData;
import com.socket.IoSession;
import com.util.ActionUtils;
import com.util.IOUtils;
import com.util.ZlibUtil;
import com.web.WebHandler;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
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
        TimeUnit.SECONDS.sleep(5);
    }

    @SneakyThrows
    @Test
    public void zlibTest() {
        for (int j = 0; j < 2; j++) {
            String str = "welcome toto welcome toto " + j;
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            System.out.println(bytes.length);
            byte[] byte2 = ZlibUtil.compress(bytes);
            System.out.println(byte2.length);

            byte[] byte3 = ZlibUtil.decompress(byte2);
            System.out.println(new String(byte3));
        }
    }

    @SneakyThrows
    @Test
    public void zlibThreadTest() {
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            new Thread(() -> {
                for (int j = 0; j < 2; j++) {
                    String str = "welcome toto welcome toto " + finalI + "_" + j;
                    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
                    System.out.println(bytes.length);
                    try {
                        byte[] byte2 = ZlibUtil.compress(bytes);
                        System.out.println(byte2.length);

                        byte[] byte3 = ZlibUtil.decompress(byte2);
                        System.out.println(new String(byte3));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
        TimeUnit.SECONDS.sleep(5);
    }

}
