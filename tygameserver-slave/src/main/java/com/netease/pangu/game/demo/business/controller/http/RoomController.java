package com.netease.pangu.game.demo.business.controller.http;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.common.meta.IAvatar;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.rpc.WsRpcResponse;
import com.netease.pangu.game.rpc.annotation.WsRpcCall;
import com.netease.pangu.game.rpc.annotation.WsRpcController;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.AvatarSessionService;
import com.netease.pangu.game.service.RoomService;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

@WsRpcController("/room")
public class RoomController {
	@Resource private AvatarSessionService avatarSessionService;
	@Resource private AvatarService avatarService;
	@Resource private RoomService roomService;
	
	@WsRpcCall("/list")
	public GameResult listRoom(GameContext<Avatar> ctx){
		GameResult result = ReturnUtils.succ(roomService.getRooms());
		return result;
	}
	
	@WsRpcCall("/create")
	public GameResult createRoom(long gameId, int maxSize, GameContext<AvatarSession<Avatar>> ctx){
		AvatarSession<Avatar> session = ctx.getSession();
		long roomId = roomService.createRoom(gameId, session.getAvatarId(), maxSize);
		GameResult result;
		if(roomId > 0){
			session.getAvatar().setState(Avatar.READY);
			result = ReturnUtils.succ(roomId);
		}else{
			result = ReturnUtils.failed("create room failed");
		}
		return result;
	}
	
	@WsRpcCall("/join")
	public GameResult joinRoom(long roomId, GameContext<AvatarSession<Avatar>> ctx){
		AvatarSession<Avatar> session = ctx.getSession();
		boolean isOk = roomService.joinRoom(session.getAvatarId(), roomId);
		GameResult result;
		if(isOk){
			roomService.broadcast(roomId, getRoom(roomId), "/room/info");
			result = ReturnUtils.succ(roomId);
		}else{
			result = ReturnUtils.failed(String.format("failed to join %d", roomId));
		}
		return result;
	}
	
	private GameResult getRoomInfo(long roomId){
		GameRoom room = roomService.getGameRoom(roomId);
		Map<String, Object> payload = new HashMap<String, Object>();
		Map<Long, Avatar>  players = avatarSessionService.getAvatars(room.getSessionIds());
		payload.put("members", players);
		payload.put("id", room.getId());
		payload.put("state", room.getStatus().ordinal());
		payload.put("maxSize", room.getMaxSize());
		payload.put("count", room.getSessionIds().size());
		payload.put("owner", avatarSessionService.getSession(room.getOwnerId()).getName());
		GameResult result = ReturnUtils.succ(payload);
		return result;
	}
	
	@WsRpcCall("/info")
	public GameResult getRoom(long roomId){
		return getRoomInfo(roomId);
	}
	
	@WsRpcCall("/chat")
	public void chat(long roomId, String msg, GameContext<AvatarSession<Avatar>> ctx){
		AvatarSession<Avatar> pSession = ctx.getSession();
		GameRoom room = roomService.getGameRoom(roomId);
		Map<Long, AvatarSession<Avatar>> members = avatarSessionService.getAvatarSesssions(room.getSessionIds());
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("msg", msg);
		Map<String, Object> source = new HashMap<String, Object>();
		source.put("uuid", pSession.getUuid());
		IAvatar avatar = pSession.getAvatar();
		source.put("avatarName", avatar.getName());
		GameResult result = ReturnUtils.succ(payload, source);		
		for(AvatarSession<Avatar> member: members.values()){
			if(member.getChannel()!= null && member.getChannel().isActive()){
				WsRpcResponse response = WsRpcResponse.create(ctx.getRpcMethodName());
				response.setContent(result);
				member.sendJSONMessage(response);
			}
		}
	}
	
}
