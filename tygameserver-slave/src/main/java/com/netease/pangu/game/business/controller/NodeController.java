package com.netease.pangu.game.business.controller;

import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;
import com.netease.pangu.game.service.RoomService;

import javax.annotation.Resource;
import java.util.Map;

@HttpController(value = "/slave", gameId = GameConst.SYSTEM)
public class NodeController {
    @Resource
    private RoomService gameRoomManager;

    @HttpRequestMapping("/room/info")
    public Map<Long, GameRoom> getRoomInfo() {
        return gameRoomManager.getRooms();
    }
}
