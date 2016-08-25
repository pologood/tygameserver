package com.netease.pangu.game.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.netease.pangu.game.annotation.NettyRpcCall;
import com.netease.pangu.game.annotation.NettyRpcController;
import com.netease.pangu.game.meta.GameContext;
import com.netease.pangu.game.meta.GameRoom;
import com.netease.pangu.game.meta.Player;
import com.netease.pangu.game.meta.PlayerSession;
import com.netease.pangu.game.service.GameRoomManager;
import com.netease.pangu.game.service.PlayerManager;
import com.netease.pangu.game.service.PlayerSessionManager;
import com.netease.pangu.game.util.JsonUtil;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

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
	
	@NettyRpcCall("room/join")
	public void joinRoom(long roomId, GameContext ctx){
		PlayerSession pSession = ctx.getPlayerSession();
		boolean isOk = gameRoomManager.joinRoom(pSession.getId(), roomId);
		GameResult result;
		if(isOk){
			GameRoom room = gameRoomManager.getGameRoom(roomId);
			Map<String, Object> payload = new HashMap<String, Object>();
			Map<Long, Player>  players = playerSessionManager.getPlayers(room.getPlayerSessionIds());
			payload.put("members", players);
			result = ReturnUtils.succ("room/join", payload);
		}else{
			result = ReturnUtils.failed("room/join", roomId);
		}
		ctx.getChannel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(result)));
	}
	
	@NettyRpcCall("room/chat")
	public void chat(long roomId, String msg, GameContext ctx){
		PlayerSession pSession = ctx.getPlayerSession();
		GameRoom room = gameRoomManager.getGameRoom(roomId);
		Map<Long, PlayerSession> members = playerSessionManager.getPlayerSesssions(room.getPlayerSessionIds());
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("msg", msg);
		Map<String, Object> source = new HashMap<String, Object>();
		source.put("sessionId", pSession.getId());
		Player player = pSession.getPlayer();
		source.put("playerName", player.getName());
		GameResult result = ReturnUtils.succ("room/chat", payload, source);		
		for(PlayerSession member: members.values()){
			if(member.getChannel()!= null && member.getChannel().isActive()){
				member.sendMessage(new TextWebSocketFrame(JsonUtil.toJson(result)));
			}
		}
	}
	
}
