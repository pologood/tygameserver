package com.netease.pangu.game.demo.business.controller.http;

import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;

@HttpController("/slave")
public class SlaveController {
	@HttpRequestMapping("/room")
	public String getRoom(int i, int j){
		return "asdfasf";
	}
}
