package com.netease.pangu.game.service;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.netease.pangu.game.meta.PlayerSession;

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

	public PlayerSession createPlayerSession(long playerId, Channel channel) {
		PlayerSession playerSession = new PlayerSession();
		playerSession.setPlayerId(playerId);
		playerSession.setAttrs(new HashMap<String, Object>());
		playerSession.setRoomId(0L);
		playerSession.setPlayerId(uniqueIdGeneratorService.generate());
		sessions.put(playerSession.getPlayerId(), playerSession);
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
