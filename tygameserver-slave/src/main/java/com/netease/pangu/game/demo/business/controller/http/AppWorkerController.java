package com.netease.pangu.game.demo.business.controller.http;

import java.util.Map;

import javax.annotation.Resource;

import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;
import com.netease.pangu.game.service.GameRoomManager;

@HttpController("/slave")
public class AppWorkerController {
	@Resource private GameRoomManager gameRoomManager;
	
	@HttpRequestMapping("/room/info")
	public Map<Long, GameRoom> getRoomInfo(){
		return gameRoomManager.getRooms();
	}
}
