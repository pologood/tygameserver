package com.netease.pangu.game.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import com.netease.pangu.game.annotation.NettyRpcCall;
import com.netease.pangu.game.annotation.NettyRpcController;
import com.netease.pangu.game.controller.BusinessCode.GameResult;
import com.netease.pangu.game.meta.GameContext;
import com.netease.pangu.game.meta.Player;
import com.netease.pangu.game.meta.PlayerSession;
import com.netease.pangu.game.service.GameRoomManager;
import com.netease.pangu.game.service.PlayerManager;
import com.netease.pangu.game.service.PlayerSessionManager;
import com.netease.pangu.game.util.JsonUtil;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

@NettyRpcController
public class PlayerController {
	@Resource private PlayerSessionManager playerSessionManager;
	@Resource private PlayerManager playerManager;
	@Resource private GameRoomManager gameRoomManager;
	
	@NettyRpcCall("player/reg")
	public void register(String name, GameContext ctx){
		Player player = playerManager.createPlayer(name);
		PlayerSession playerSession = playerSessionManager.createPlayerSession(player, ctx.getChannel());
		GameResult result = new GameResult();
		result.setCode(BusinessCode.SUCC);
		result.setRpcMethodName("player/reg");
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("sessionId", playerSession.getId());
		payload.put("roleName", name);
		result.setPayload(payload);
		playerSession.setChannel(ctx.getChannel());
		playerSession.sendMessage(new TextWebSocketFrame(JsonUtil.toJson(result)));
	}
	
	@NettyRpcCall("player/login")
	public void login(long playerSessionId, GameContext ctx){
		PlayerSession playerSession = ctx.getPlayerSession();
		if(playerSession.getChannel() != null && playerSession.getChannel().isActive()){
			GameResult result = new GameResult();
			result.setCode(BusinessCode.FAILED);
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("msg", "user is logined");
			result.setPayload(payload);
			result.setRpcMethodName("player/login");
			ctx.getChannel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(result)));
			return;
		} else {
			GameResult result = new GameResult();
			result.setCode(BusinessCode.SUCC);
			result.setRpcMethodName("player/login");
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("sessionId", playerSession.getId());
			payload.put("roleName", playerSession.getPlayer().getName());
			result.setPayload(payload);
			playerSession.setChannel(ctx.getChannel());
			playerSession.sendMessage(new TextWebSocketFrame(JsonUtil.toJson(result)));
		}
	}
	
	@NettyRpcCall("player/list")
	public void list(GameContext ctx){
		TreeMap<Long, Player> map = new TreeMap<Long, Player>();
		for(Long sesssionId : playerSessionManager.getPlayerSessions().keySet()){
			map.put(sesssionId, playerSessionManager.getPlayerSessions().get(sesssionId).getPlayer());
		}
		GameResult result = new GameResult();
		result.setCode(BusinessCode.SUCC);
		result.setRpcMethodName("player/list");
		result.setPayload(map);
		ctx.getChannelHandlerContext().channel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(result)));		
	}
	
	@NettyRpcCall("player/chat")
	public void chat(long sessionId, String msg){
		PlayerSession playerSession = playerSessionManager.getSession(sessionId);
		GameResult result = new GameResult();
		result.setCode(BusinessCode.SUCC);
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("msg", msg);
		result.setPayload(msg);
		result.setRpcMethodName("chat");
		Map<String, Object> target = new HashMap<String, Object>();
		target.put("sessionId", playerSession.getId());
		Player player = playerSession.getPlayer();
		target.put("playerName", player.getName());
		result.setTarget(target);
		playerSession.sendMessage(new TextWebSocketFrame(JsonUtil.toJson(result)));
	}
	
	@NettyRpcCall("room/create")
	public void createRoom(long gameId, int maxSize,GameContext ctx){
		PlayerSession pSession = ctx.getPlayerSession();
		long roomId = gameRoomManager.createRoom(gameId, pSession.getId(), maxSize);
		
	}
	
}
