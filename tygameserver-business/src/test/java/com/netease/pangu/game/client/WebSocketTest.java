package com.netease.pangu.game.client;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.netease.pangu.game.util.JsonUtil;

public class WebSocketTest {
	private final static Logger logger = Logger.getLogger(WebSocketTest.class);
	public static class EventSocket extends WebSocketAdapter
	{
	    @Override
	    public void onWebSocketConnect(Session sess)
	    {
	        super.onWebSocketConnect(sess);
	        System.out.println("Socket Connected: " + sess);
	    }
	    
	    @Override
	    public void onWebSocketText(String message)
	    {
	        super.onWebSocketText(message);
	        System.out.println("Received TEXT message: " + message);
	        logger.info(message);
	    }
	    
	    @Override
	    public void onWebSocketClose(int statusCode, String reason)
	    {
	        super.onWebSocketClose(statusCode,reason);
	        System.out.println("Socket Closed: [" + statusCode + "] " + reason);
	    }
	    
	    @Override
	    public void onWebSocketError(Throwable cause)
	    {
	        super.onWebSocketError(cause);
	        cause.printStackTrace(System.err);
	    }
	}
	
	public static class WebSocketDemoClient implements Runnable{
		private String name;
		private WebSocketClient client;
		private URI uri = URI.create("ws://localhost:8080/websocket");
		public WebSocketDemoClient(String name) {
			this.name = name;
		}
		@Override
		public void run() {
			client = new WebSocketClient();
			try {
				client.start();
			      // The socket that receives events
	            EventSocket socket = new EventSocket();
	            // Attempt Connect
	            Future<Session> fut = client.connect(socket,uri);
	            // Wait for Connect
	            Session session = fut.get();
	            // Send a message
	            Map<String, Object> obj = new HashMap<String, Object>();
	            obj.put("rpcMethod","/master/avatar");	
	            Map<String, Object> params = new HashMap<String, Object>();
	            params.put("gameId", 1);
	            params.put("uuid", 100041);
	            obj.put("params", params);
	            session.getRemote().sendString(JsonUtil.toJson(obj));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
	
	public static void main(String[] args) {
		for(int i = 0; i  <= 1; i ++){
			Thread thread = new Thread(new WebSocketDemoClient("a" + i));
			thread.start();
		}
	}
}

	
