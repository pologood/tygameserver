package com.netease.pangu.game.controller;

import javax.annotation.Resource;

import com.netease.pangu.game.annotation.NettyRpcCall;
import com.netease.pangu.game.annotation.NettyRpcController;
import com.netease.pangu.game.controller.ReturnUtils.GameResult;
import com.netease.pangu.game.meta.GameContext;
import com.netease.pangu.game.meta.PlayerSession;
import com.netease.pangu.game.service.GameRoomManager;
import com.netease.pangu.game.service.PlayerManager;
import com.netease.pangu.game.service.PlayerSessionManager;
import com.netease.pangu.game.util.JsonUtil;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

@NettyRpcController
public class RoomController {
	@Resource private PlayerSessionManager playerSessionManager;
	@Resource private PlayerManager playerManager;
	@Resource private GameRoomManager gameRoomManager;
	
	@NettyRpcCall("room/list")
	public void listRoom(GameContext ctx){
		GameResult result = ReturnUtils.succ("room/list", gameRoomManager.getRooms());
		ctx.getChannel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(result)));
	}
	
	@NettyRpcCall("room/create")
	public void createRoom(long gameId, int maxSize,GameContext ctx){
		PlayerSession pSession = ctx.getPlayerSession();
		long roomId = gameRoomManager.createRoom(gameId, pSession.getId(), maxSize);
		GameResult result = ReturnUtils.succ("room/create", roomId);
		ctx.getChannel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(result)));
	}
}
