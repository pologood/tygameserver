package com.netease.pangu.game.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.netease.pangu.game.dao.PlayerDao;
import com.netease.pangu.game.distribution.Node;
import com.netease.pangu.game.meta.Player;

@Component
public class PlayerManager extends AbstractPlayerManager<Player> {
	@Resource private PlayerDao playerDao;
	private Node currentNode;
	@Override
	protected Player put(long playerId, Player player) {
		Player p = playerDao.getPlayer(playerId);
		if(p == null){
			playerDao.addPlayer(player);
			p = playerDao.getPlayer(playerId);
		}
		return p;
	}

	public Player getPlayerByUUID(String uuid){
		return playerDao.getPlayerByUUID(uuid);
	}
	
	public Player getPlayerByName(String name){
		return playerDao.getPlayerByName(name);
	}
	
	@Override
	protected Player get(long playerId) {
		return playerDao.getPlayer(playerId);
	}

	public Node getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(Node node) {
		this.currentNode = node;
	}

}
