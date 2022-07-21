package com;

import com.decoder.Rijndael;
import com.socket.ActionData;
import com.socket.IoSession;
import com.util.ActionUtils;
import com.web.WebHandler;
import lombok.SneakyThrows;
import org.junit.Test;

import javax.crypto.Cipher;

public class JavaTest {

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
    public void cipherTest() {


        for (int i = 0; i < 5; i++) {

            System.out.println(Cipher.getInstance(Rijndael.AESMode.CBC_PKCS5.getName()));

        }




    }

}
