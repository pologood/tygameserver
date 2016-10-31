package com.netease.pangu.game.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import com.netease.pangu.game.common.meta.IPlayer;

public abstract class AbstractPlayerManager<P extends IPlayer> {
	private final Map<Long, P> players;
	@Resource
	private UniqueIDGeneratorService uniqueIdGeneratorService;
	
	public AbstractPlayerManager(){
		 players = new ConcurrentHashMap<Long, P>();
	}
	
	public P createPlayer(P player){
		long playerId = uniqueIdGeneratorService.generatePlayerId();
		player.setId(playerId);
		if(players.put(playerId, player) == null){
			return player;
		}
		return null;
	}
	
	public boolean remove(long key){
		if(players.remove(key) != null){
			return true;
		}
		return false;
	}
	
	public P get(long key){
		return players.get(key);
	}
}
