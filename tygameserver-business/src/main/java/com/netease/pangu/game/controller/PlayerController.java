package com.netease.pangu.game.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import com.netease.pangu.game.common.meta.GameContext;
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
public class PlayerController {
	@Resource private PlayerSessionManager playerSessionManager;
	@Resource private PlayerManager playerManager;
	@Resource private GameRoomManager gameRoomManager;
	
	@NettyRpcCall("player/reg")
	public void register(String name, GameContext ctx){
		Player player = playerManager.createPlayer(name);
		PlayerSession playerSession = playerSessionManager.createPlayerSession(player, ctx.getChannel());
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("sessionId", playerSession.getId());
		payload.put("roleName", name);
		GameResult result = ReturnUtils.succ("player/reg", payload);
		playerSession.setChannel(ctx.getChannel());
		playerSession.sendMessage(new TextWebSocketFrame(JsonUtil.toJson(result)));
	}
	
	@NettyRpcCall("player/login")
	public void login(long playerSessionId, GameContext ctx){
		PlayerSession playerSession = ctx.getPlayerSession();
		if(playerSession.getChannel() != null && playerSession.getChannel().isActive()){
			GameResult result = ReturnUtils.failed("player/login", "user has logined");
			ctx.getChannel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(result)));
			return;
		} else {
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("sessionId", playerSession.getId());
			payload.put("roleName", playerSession.getPlayer().getName());
			GameResult result = ReturnUtils.succ("player/login", payload);
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
		GameResult result = ReturnUtils.succ("player/list", map);
		ctx.getChannelHandlerContext().channel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(result)));		
	}
	
	@NettyRpcCall("player/chat")
	public void chat(long sessionId, String msg){
		PlayerSession playerSession = playerSessionManager.getSession(sessionId);
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("msg", msg);
		Map<String, Object> source = new HashMap<String, Object>();
		source.put("sessionId", playerSession.getId());
		Player player = playerSession.getPlayer();
		source.put("playerName", player.getName());
		GameResult result = ReturnUtils.succ("player/chat", payload, source);
		playerSession.sendMessage(new TextWebSocketFrame(JsonUtil.toJson(result)));
	}
	
}
