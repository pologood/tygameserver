package com.netease.pangu.game.controller.slave;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.common.meta.IPlayer;
import com.netease.pangu.game.common.meta.PlayerSession;
import com.netease.pangu.game.meta.Player;
import com.netease.pangu.game.rpc.WsRpcResponse;
import com.netease.pangu.game.rpc.annotation.WsRpcCall;
import com.netease.pangu.game.rpc.annotation.WsRpcController;
import com.netease.pangu.game.service.GameRoomManager;
import com.netease.pangu.game.service.PlayerManager;
import com.netease.pangu.game.service.PlayerSessionManager;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

@WsRpcController("/room")
public class RoomController {
	@Resource private PlayerSessionManager playerSessionManager;
	@Resource private PlayerManager playerManager;
	@Resource private GameRoomManager gameRoomManager;
	
	@WsRpcCall("/list")
	public GameResult listRoom(GameContext<Player> ctx){
		GameResult result = ReturnUtils.succ(gameRoomManager.getRooms());
		return result;
	}
	
	@WsRpcCall("/create")
	public GameResult createRoom(long gameId, int maxSize,GameContext<Player> ctx){
		PlayerSession<Player> pSession = ctx.getPlayerSession();
		long roomId = gameRoomManager.createRoom(gameId, pSession.getId(), maxSize);
		GameResult result;
		if(roomId > 0){
			result = ReturnUtils.succ(roomId);
		}else{
			result = ReturnUtils.failed("create room failed");
		}
		return result;
	}
	
	@WsRpcCall("/join")
	public GameResult joinRoom(long roomId, GameContext<Player> ctx){
		PlayerSession<Player> pSession = ctx.getPlayerSession();
		boolean isOk = gameRoomManager.joinRoom(pSession.getId(), roomId);
		GameResult result;
		if(isOk){
			result = ReturnUtils.succ(roomId);
		}else{
			result = ReturnUtils.failed(String.format("failed to join %d", roomId));
		}
		return result;
	}
	
	@WsRpcCall("/info")
	public GameResult getRoom(long roomId, GameContext<Player> ctx){
		GameRoom room = gameRoomManager.getGameRoom(roomId);
		Map<String, Object> payload = new HashMap<String, Object>();
		Map<Long, Player>  players = playerSessionManager.getPlayers(room.getPlayerSessionIds());
		payload.put("members", players);
		payload.put("id", room.getId());
		payload.put("state", room.getStatus().ordinal());
		payload.put("maxSize", room.getMaxSize());
		payload.put("count", room.getPlayerSessionIds().size());
		payload.put("owner", playerSessionManager.getSession(room.getOwnerId()).getPlayer().getName());
		GameResult result = ReturnUtils.succ(payload);
		return result;
	}
	
	@WsRpcCall("/chat")
	public void chat(long roomId, String msg, GameContext<Player> ctx){
		PlayerSession<Player> pSession = ctx.getPlayerSession();
		GameRoom room = gameRoomManager.getGameRoom(roomId);
		Map<Long, PlayerSession<Player>> members = playerSessionManager.getPlayerSesssions(room.getPlayerSessionIds());
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("msg", msg);
		Map<String, Object> source = new HashMap<String, Object>();
		source.put("sessionId", pSession.getId());
		IPlayer player = pSession.getPlayer();
		source.put("playerName", player.getName());
		GameResult result = ReturnUtils.succ(payload, source);		
		for(PlayerSession<Player> member: members.values()){
			if(member.getChannel()!= null && member.getChannel().isActive()){
				WsRpcResponse response = WsRpcResponse.create(ctx.getRpcMethodName());
				response.setContent(result);
				member.sendJSONMessage(response);
			}
		}
	}
	
}
