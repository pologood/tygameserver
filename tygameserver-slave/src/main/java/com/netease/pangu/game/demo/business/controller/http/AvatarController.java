package com.netease.pangu.game.demo.business.controller.http;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.distribution.service.SystemAttrService;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.rpc.WsRpcResponse;
import com.netease.pangu.game.rpc.annotation.WsRpcCall;
import com.netease.pangu.game.rpc.annotation.WsRpcController;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.AvatarSessionService;
import com.netease.pangu.game.service.RoomService;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

@WsRpcController("/avatar")
public class AvatarController {
	@Resource private AvatarSessionService avatarSessionService;
	@Resource private AvatarService avatarService;
	@Resource private RoomService roomService;
	@Resource private SystemAttrService systemAttrService;
	
	@WsRpcCall("/list")
	public GameResult list(GameContext<AvatarSession<Avatar>> ctx){
		TreeMap<Long, Avatar> map = new TreeMap<Long, Avatar>();
		for(Long avatarId : avatarSessionService.getSessions().keySet()){
			map.put(avatarId, avatarSessionService.getSessions().get(avatarId).getAvatar());
		}
		GameResult result = ReturnUtils.succ(map);
		return result;	
	}
	
	@WsRpcCall("/ready")
	public GameResult ready(GameContext<AvatarSession<Avatar>> ctx){
		AvatarSession<Avatar> session = ctx.getSession();
		if(session.getState() != AvatarSession.READY){
			session.setState(AvatarSession.READY);
			roomService.broadcast(RoomService.ROOM_INFO, session.getRoomId(), roomService.getRoomInfo(session.getRoomId()));
		}
		GameResult result = ReturnUtils.succ("ready go");
		return result;	
	}
	
	@WsRpcCall("/chat")
	public void chat(long sessionId, String msg, GameContext<AvatarSession<Avatar>> context){
		AvatarSession<Avatar> session = avatarSessionService.getSession(sessionId);
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("msg", msg);
		Map<String, Object> source = new HashMap<String, Object>();
		source.put("uuid", context.getSession().getUuid());
		source.put("playerName", context.getSession().getName());
		GameResult result = ReturnUtils.succ(payload, source);
		WsRpcResponse response = WsRpcResponse.create(context.getRpcMethodName());
		response.setContent(result);
		session.sendJSONMessage(response);
	}
	
}
