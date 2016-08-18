package com.netease.pangu.game.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.netease.pangu.game.app.PlayerSession;

@Component
public class PlayerSessionManager {
	private final Map<Object, PlayerSession> sessions;

	public PlayerSessionManager(){
		sessions = new ConcurrentHashMap<Object, PlayerSession>();
	}
	
	public boolean put(Object key, PlayerSession room) {
		if (key == null || room == null) {
			return false;
		}
		if (sessions.put(room, room) == null) {
			return true;
		}
		return false;
	}

	public boolean remove(Object key) {
		if (key == null) {
			return false;
		}
		if (sessions.remove(key) != null) {
			return true;
		}
		return false;
	}

	public PlayerSession get(Object key) {
		return sessions.get(key);
	}
}
