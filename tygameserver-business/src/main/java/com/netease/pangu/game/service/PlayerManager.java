package com.netease.pangu.game.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.netease.pangu.game.meta.Player;

@Component
public class PlayerManager {
	private final Map<Long, Player> players;
	@Resource
	private UniqueIDGeneratorService uniqueIdGeneratorService;
	
	public PlayerManager(){
		 players = new ConcurrentHashMap<Long, Player>();
	}
	
	public Player createPlayer(String name){
		Player player  = new Player();
		long playerId = uniqueIdGeneratorService.generatePlayerId();
		player.setId(playerId);
		player.setName(name);
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
	
	public Player get(long key){
		return players.get(key);
	}
}
