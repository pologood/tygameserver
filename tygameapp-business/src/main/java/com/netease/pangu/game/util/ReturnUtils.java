package com.netease.pangu.game.util;

public class ReturnUtils {
	public final static int SUCC = 1;
	public final static int FAILED = 0;
	
	public static GameResult succ(String rpcMethodName, String message){
		return succ(rpcMethodName, null, null , message);
	}
	
	public static GameResult succ(String rpcMethodName, Object payload){
		return succ(rpcMethodName, payload, null, null);
	}
	
	public static GameResult succ(String rpcMethodName, Object payload, String message){
		return succ(rpcMethodName, payload, null);
	}
	
	public static GameResult succ(String rpcMethodName, Object payload, Object source){
		return succ(rpcMethodName, payload, source , null);
	}
	
	public static GameResult succ(String rpcMethodName, Object payload, Object source, String message){
		GameResult result = new GameResult();
		result.setCode(SUCC);
		result.setPayload(payload);
		result.setRpcMethodName(rpcMethodName);
		result.setSource(source);
		result.setMessage(message);
		return result;
	}
	
	public static GameResult failed(String rpcMethodName, Object payload, Object source, String message){
		GameResult result = new GameResult();
		result.setCode(FAILED);
		result.setPayload(payload);
		result.setRpcMethodName(rpcMethodName);
		result.setSource(source);
		result.setMessage(message);
		return result;
	}
	
	public static GameResult failed(String rpcMethodName, Object payload, String message){
		return failed(rpcMethodName, payload, null, message);
	}
	
	public static GameResult failed(String rpcMethodName, String message){
		return failed(rpcMethodName, null, null, message);
	}
	
	public static class GameResult {
		private String rpcMethodName;
		private int code;
		private Object payload;
		private Object source;
		private String message;
		public String getRpcMethodName() {
			return rpcMethodName;
		}

		public void setRpcMethodName(String rpcMethodName) {
			this.rpcMethodName = rpcMethodName;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public Object getPayload() {
			return payload;
		}

		public void setPayload(Object payload) {
			this.payload = payload;
		}

		public Object getSource() {
			return source;
		}

		public void setSource(Object source) {
			this.source = source;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
		
	}
	
}
