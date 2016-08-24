package com.netease.pangu.game.controller;

import javax.annotation.Resource;

import com.netease.pangu.game.common.NettyRpcCall;
import com.netease.pangu.game.common.NettyRpcController;
import com.netease.pangu.game.meta.Player;
import com.netease.pangu.game.meta.PlayerSession;
import com.netease.pangu.game.service.PlayerManager;
import com.netease.pangu.game.service.PlayerSessionManager;

@NettyRpcController
public class PlayerController {
	@Resource PlayerSessionManager playerSessionManager;
	@Resource PlayerManager playerManager;
	@NettyRpcCall("create")
	public PlayerSession create(String name){
		Player player = playerManager.createPlayer(name);
		return playerSessionManager.createPlayerSession(player.getId(), null);
	}
	
}
