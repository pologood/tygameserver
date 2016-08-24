package com.netease.pangu.game.controller;

import com.netease.pangu.game.annotation.NettyRpcCall;
import com.netease.pangu.game.annotation.NettyRpcController;

@NettyRpcController
public class Test2Controller {
	
	@NettyRpcCall("t1")
	public void test1(){
		
	}
	
	@NettyRpcCall("t2")
	public void test2(){
		
	}
}
