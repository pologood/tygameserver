package com.netease.pangu.game.business.controller;

import java.util.Map;

import javax.annotation.Resource;

import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;
import com.netease.pangu.game.service.RoomService;

@HttpController("/slave")
public class NodeController {
	@Resource private RoomService gameRoomManager;
	
	@HttpRequestMapping("/room/info")
	public Map<Long, GameRoom> getRoomInfo(){
		return gameRoomManager.getRooms();
	}
}
