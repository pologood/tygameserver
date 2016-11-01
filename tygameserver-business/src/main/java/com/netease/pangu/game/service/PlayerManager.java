package com.netease.pangu.game.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.netease.pangu.game.dao.PlayerDao;
import com.netease.pangu.game.meta.Player;

@Component
public class PlayerManager extends AbstractPlayerManager<Player> {
	@Resource private PlayerDao playerDao;
	
	@Override
	protected Player put(long playerId, Player player) {
		Player p = playerDao.getPlayer(playerId);
		if(p == null){
			playerDao.addPlayer(player);
			p = playerDao.getPlayer(playerId);
		}
		return p;
	}

	@Override
	protected Player get(long playerId) {
		return playerDao.getPlayer(playerId);
	}

}
