package com.netease.pangu.game.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.netease.pangu.game.app.Player;

@Component
public class PlayerManager {
	private final Map<Object, Player> players;
	
	public PlayerManager(){
		 players = new ConcurrentHashMap<Object, Player>();
	}
	
	public boolean put(Object key , Player player){
		if(key == null || player == null){
			return false;
		}
		if(players.put(key, player) == null){
			return true;
		}
		return false;
	}
	
	public boolean remove(Object key){
		if(key == null){
			return false;
		}
		if(players.remove(key) != null){
			return true;
		}
		return false;
	}
	
	public Player get(Object key){
		return players.get(key);
	}
}
