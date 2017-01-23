package com.netease.pangu.game.test;

import com.netease.pangu.game.util.WebSocketUtil;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.HashMap;

public class AvatarTest {
    private final static Logger logger = Logger.getLogger(AvatarTest.class);

    @Test
    public void listTest() {
        WebSocketUtil.Client socket = new WebSocketUtil.Client("ws://127.0.0.1:8091/ws", new WebSocketUtil.Handler() {
            @Override
            public void call(String msg) {
                System.out.println(msg);
            }
        });
        socket.sendString("/avatar/list", "1", 1, new HashMap<String, Object>());
    }
}
