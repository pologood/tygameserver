package com.netease.pangu.game.controller.slave;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.common.meta.PlayerSession;
import com.netease.pangu.game.distribution.Node;
import com.netease.pangu.game.meta.Player;
import com.netease.pangu.game.rpc.WsRpcResponse;
import com.netease.pangu.game.rpc.annotation.WsRpcCall;
import com.netease.pangu.game.rpc.annotation.WsRpcController;
import com.netease.pangu.game.service.GameRoomManager;
import com.netease.pangu.game.service.PlayerManager;
import com.netease.pangu.game.service.PlayerSessionManager;
import com.netease.pangu.game.util.NettyHttpUtil;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

@WsRpcController("/player")
public class PlayerController {
	@Resource private PlayerSessionManager playerSessionManager;
	@Resource private PlayerManager playerManager;
	@Resource private GameRoomManager gameRoomManager;
	
	@WsRpcCall("/reg")
	public GameResult register(String name, String uuid, GameContext<Player> ctx){
		Player player = new Player();
		player.setName(name);
		player.setUuid(uuid);
		Node node = playerManager.getCurrentNode();
		player.setServer(node.getName());
		long current = System.currentTimeMillis();
		player.setWriteToDbTime(current);
		player.setLastLoginTime(current);
		player = playerManager.createPlayer(player);
		
		PlayerSession<Player> playerSession = playerSessionManager.createPlayerSession(player, ctx.getChannel());
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("sessionId", playerSession.getId());
		payload.put("roleName", name);
		return ReturnUtils.succ(payload);
	}
	
	@WsRpcCall("/login")
	public void login(long playerSessionId, GameContext<Player> ctx){
		PlayerSession<Player> playerSession = (PlayerSession<Player>)ctx.getPlayerSession();
		if(playerSession.getChannel() != null && playerSession.getChannel().isActive()){
			GameResult result = ReturnUtils.failed("user has logined");
			NettyHttpUtil.sendWsResponse(ctx, playerSession.getChannel(), result);
			return;
		} else {
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("sessionId", playerSession.getId());
			payload.put("roleName", playerSession.getPlayer().getName());
			GameResult result = ReturnUtils.succ(payload);
			playerSession.setChannel(ctx.getChannel());
			NettyHttpUtil.sendWsResponse(ctx, playerSession.getChannel(), result);
		}
	}
	
	@WsRpcCall("/list")
	public GameResult list(GameContext<Player> ctx){
		TreeMap<Long, Player> map = new TreeMap<Long, Player>();
		for(Long sesssionId : playerSessionManager.getPlayerSessions().keySet()){
			map.put(sesssionId, playerSessionManager.getPlayerSessions().get(sesssionId).getPlayer());
		}
		GameResult result = ReturnUtils.succ(map);
		return result;	
	}
	
	@WsRpcCall("/chat")
	public void chat(long sessionId, String msg, GameContext<Player> context){
		PlayerSession<Player> playerSession = playerSessionManager.getSession(sessionId);
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("msg", msg);
		Map<String, Object> source = new HashMap<String, Object>();
		source.put("sessionId", context.getPlayerSession().getId());
		source.put("playerName", context.getPlayerSession().getPlayer().getName());
		GameResult result = ReturnUtils.succ(payload, source);
		WsRpcResponse response = WsRpcResponse.create(context.getRpcMethodName());
		response.setContent(result);
		playerSession.sendJSONMessage(response);
	}
	
}
