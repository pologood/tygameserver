package com.netease.pangu.game.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.common.meta.Player;
import com.netease.pangu.game.common.meta.PlayerSession;
import com.netease.pangu.game.rpc.annotation.NettyRpcCall;
import com.netease.pangu.game.rpc.annotation.NettyRpcController;
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
		GameResult result;
		if(roomId > 0){
			result = ReturnUtils.succ("room/create", roomId);
		}else{
			result = ReturnUtils.failed("room/create", "create room failed");
		}
		ctx.getChannel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(result)));
	}
	
	@NettyRpcCall("room/join")
	public void joinRoom(long roomId, GameContext ctx){
		PlayerSession pSession = ctx.getPlayerSession();
		boolean isOk = gameRoomManager.joinRoom(pSession.getId(), roomId);
		GameResult result;
		if(isOk){
			result = ReturnUtils.succ("room/join", roomId);
		}else{
			result = ReturnUtils.failed("room/join", String.format("failed to join %d", roomId));
		}
		ctx.getChannel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(result)));
	}
	
	@NettyRpcCall("room/info")
	public void getRoom(long roomId, GameContext ctx){
		GameRoom room = gameRoomManager.getGameRoom(roomId);
		Map<String, Object> payload = new HashMap<String, Object>();
		Map<Long, Player>  players = playerSessionManager.getPlayers(room.getPlayerSessionIds());
		payload.put("members", players);
		payload.put("id", room.getId());
		payload.put("state", room.getStatus().ordinal());
		payload.put("maxSize", room.getMaxSize());
		payload.put("count", room.getPlayerSessionIds().size());
		payload.put("owner", playerSessionManager.getSession(room.getOwnerId()).getPlayer().getName());
		GameResult result = ReturnUtils.succ("room/info", payload);
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
