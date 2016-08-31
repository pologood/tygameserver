package com.netease.pangu.game.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.netease.pangu.game.common.meta.Player;
import com.netease.pangu.game.common.meta.PlayerSession;

import io.netty.channel.Channel;

@Component
public class PlayerSessionManager {
	@Resource
	private UniqueIDGeneratorService uniqueIdGeneratorService;

	public interface SessionCallable<T> {
		public T call(PlayerSession playerSession);
	}

	private final ConcurrentMap<Long, PlayerSession> sessions;

	public PlayerSessionManager() {
		sessions = new ConcurrentHashMap<Long, PlayerSession>();
	}
	
	public Map<Long, PlayerSession> getPlayerSessions(){
		return Collections.unmodifiableMap(sessions);
	}
	
	public Map<Long, Player> getPlayers(Set<Long> sessionIds){
		Map<Long, Player> playerMap = new HashMap<Long, Player>();
		for(Long sessionId: sessionIds){
			PlayerSession session = sessions.get(sessionId);
			playerMap.put(sessionId, session.getPlayer());
		}
		return Collections.unmodifiableMap(playerMap);
	}

	public Map<Long, PlayerSession> getPlayerSesssions(Set<Long> sessionIds){
		Map<Long, PlayerSession> playerMap = new HashMap<Long, PlayerSession>();
		for(Long sessionId: sessionIds){
			PlayerSession session = sessions.get(sessionId);
			playerMap.put(sessionId, session);
		}
		return Collections.unmodifiableMap(playerMap);
	}
	
	public boolean put(long sessionId, PlayerSession session) {
		if (sessions.put(sessionId, session) == null) {
			return true;
		}
		return false;
	}

	public boolean remove(long sessionId) {
		if (sessions.remove(sessionId) != null) {
			return true;
		}
		return false;
	}

	public PlayerSession getSession(long sessionId) {
		return sessions.get(sessionId);
	}

	public PlayerSession createPlayerSession(Player player, Channel channel) {
		PlayerSession playerSession = new PlayerSession();
		playerSession.setPlayer(player);
		playerSession.setAttrs(new HashMap<String, Object>());
		playerSession.setRoomId(0L);
		playerSession.setId(uniqueIdGeneratorService.generateSessionId());
		playerSession.setCreateTime(System.currentTimeMillis());
		sessions.put(playerSession.getId(), playerSession);
		return playerSession;
	}

	public <T> T updatePlayerSession(long playerSessionId, SessionCallable<T> callable) {
		PlayerSession playerSession = getSession(playerSessionId);
		Assert.notNull(callable);
		if (playerSession != null) {
			synchronized (playerSession) {
				try {
					return callable.call(playerSession);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			return callable.call(null);
		}
		return null;
	}
}
