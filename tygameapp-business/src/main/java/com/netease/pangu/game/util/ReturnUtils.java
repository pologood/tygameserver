package com.netease.pangu.game.util;

public class ReturnUtils {
	public final static int SUCC = 1;
	public final static int FAILED = 0;
	
	
	public static GameResult succ(String rpcMethodName, Object payload){
		return succ(rpcMethodName, payload, null);
	}
	
	public static GameResult succ(String rpcMethodName, Object payload, Object source){
		GameResult result = new GameResult();
		result.setCode(SUCC);
		result.setPayload(payload);
		result.setRpcMethodName(rpcMethodName);
		result.setSource(source);
		return result;
	}
	
	public static GameResult failed(String rpcMethodName, Object payload, Object source){
		GameResult result = new GameResult();
		result.setCode(FAILED);
		result.setPayload(payload);
		result.setRpcMethodName(rpcMethodName);
		result.setSource(source);
		return result;
	}
	
	public static GameResult failed(String rpcMethodName, Object payload){
		return failed(rpcMethodName, payload, null);
	}
	
	public static class GameResult {
		private String rpcMethodName;
		private int code;
		private Object payload;
		private Object source;
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
		
	}
	
}
