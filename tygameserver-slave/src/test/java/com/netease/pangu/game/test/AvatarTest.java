package com.netease.pangu.game.test;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.Test;

import com.netease.pangu.game.util.JsonUtil;

public class AvatarTest {
	private final static Logger logger = Logger.getLogger(AvatarTest.class);
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
	
	public static class WSSocketClient{
		private WebSocketClient client;
		private EventSocket socket;
		private URI uri;
		private Session session;
		public WSSocketClient(String wsURL){
			 client = new WebSocketClient();
			 socket = new EventSocket();
			 uri = URI.create(wsURL);
			 Future<Session> fut;
			try {
				client.start();
				fut = client.connect(socket,uri);
				session = fut.get();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		public void sendString(String rpcMethod, String uuid, long gameId, Map<String, Object> params) {
	            Map<String, Object> obj = new HashMap<String, Object>();
	            obj.put("rpcMethod",rpcMethod);	
	            obj.put("uuid", uuid);
	            obj.put("gameId", gameId);
	            obj.put("params",params);
	            try {
					session.getRemote().sendString(JsonUtil.toJson(obj));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	
	}
	@Test
	public void listTest(){
		WSSocketClient socket = new WSSocketClient("ws://localhost:8081/ws");
		socket.sendString("/avatar/list", "1", 1, new HashMap<String, Object>());
	}
	 
	
}
