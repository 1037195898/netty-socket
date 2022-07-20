package com;

import com.socket.ActionData;
import com.socket.IoSession;
import com.util.ActionUtils;
import com.web.WebHandler;
import lombok.SneakyThrows;
import org.junit.Test;

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

}
