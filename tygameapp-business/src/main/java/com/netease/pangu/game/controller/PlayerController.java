package com.netease.pangu.game.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.netease.pangu.game.annotation.NettyRpcCall;
import com.netease.pangu.game.annotation.NettyRpcController;
import com.netease.pangu.game.controller.BusinessCode.GameResult;
import com.netease.pangu.game.meta.PlayerSession;
import com.netease.pangu.game.service.PlayerManager;
import com.netease.pangu.game.service.PlayerSessionManager;
import com.netease.pangu.game.util.JsonUtil;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

@NettyRpcController
public class PlayerController {
	@Resource PlayerSessionManager playerSessionManager;
	@Resource PlayerManager playerManager;
	@NettyRpcCall("chat")
	public void chat(long playerId, String msg){
		PlayerSession playerSession = playerSessionManager.getSession(playerId);
		GameResult result = new GameResult();
		result.setCode(BusinessCode.SUCC);
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("msg", msg);
		result.setPayload(msg);
		result.setRpcMethodName("chat");
		result.setTarget(playerSession.getPlayerId());
		playerSession.sendMessage(new TextWebSocketFrame(JsonUtil.toJson(result)));
	}
	
}
