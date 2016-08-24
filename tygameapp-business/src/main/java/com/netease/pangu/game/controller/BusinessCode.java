package com.netease.pangu.game.controller;

public class BusinessCode {
	public final static int SUCC = 1;
	public final static int FAILED = 0;
	
	public static class GameResult {
		private String rpcMethodName;
		private int code;
		private Object payload;
		private Object target;
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

		public Object getTarget() {
			return target;
		}

		public void setTarget(Object target) {
			this.target = target;
		}
		
	}
	
}
