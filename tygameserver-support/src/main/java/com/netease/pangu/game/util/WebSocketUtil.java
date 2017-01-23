package com.netease.pangu.game.util;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by huangc on 2017/1/23.
 */
public class WebSocketUtil {
     public static interface Handler {
        void call(String msg);
    }

    public static class WebSocketHandler extends WebSocketAdapter {
        private Handler handler;
        public WebSocketHandler(Handler handler){
            this.handler = handler;
        }
        @Override
        public void onWebSocketConnect(Session session) {
            super.onWebSocketConnect(session);
        }

        @Override
        public void onWebSocketText(String message) {
            super.onWebSocketText(message);
            handler.call(message);
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            super.onWebSocketClose(statusCode, reason);
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            super.onWebSocketError(cause);
            cause.printStackTrace(System.err);
        }
    }

    public static class Client {
        private WebSocketClient client;
        private WebSocketHandler socketHandler;
        private URI uri;
        private Session session;

        public Client(String wsURL, Handler handler) {
            client = new WebSocketClient();
            socketHandler = new WebSocketHandler(handler);
            uri = URI.create(wsURL);
            Future<Session> fut;
            try {
                client.start();
                fut = client.connect(socketHandler, uri);
                session = fut.get();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void sendString(String rpcMethod, String uuid, long gameId, Map<String, Object> params) {
            Map<String, Object> obj = new HashMap<String, Object>();
            obj.put("rpcMethod", rpcMethod);
            obj.put("uuid", uuid);
            obj.put("gameId", gameId);
            obj.put("params", params);
            try {
                session.getRemote().sendString(JsonUtil.toJson(obj));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
