package com.netease.pangu.game.controller;

import com.netease.pangu.game.common.NettyRpcCall;
import com.netease.pangu.game.common.NettyRpcController;

@NettyRpcController
public class TestController {

	@NettyRpcCall("t3")
	public void test2(){
		
	}
	
}
