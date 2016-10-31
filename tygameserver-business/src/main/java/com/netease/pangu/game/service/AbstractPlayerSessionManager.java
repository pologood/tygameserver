package com.netease.pangu.game.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.util.Assert;

import com.netease.pangu.game.common.meta.IPlayer;
import com.netease.pangu.game.common.meta.PlayerSession;

import io.netty.channel.Channel;

public abstract class AbstractPlayerSessionManager<P extends IPlayer> {
	@Resource
	private UniqueIDGeneratorService uniqueIdGeneratorService;

	public interface SessionCallable<T, SP extends IPlayer> {
		public T call(PlayerSession<SP> playerSession);
	}

	private final ConcurrentMap<Long, PlayerSession<P>> sessions;

	public AbstractPlayerSessionManager() {
		sessions = new ConcurrentHashMap<Long, PlayerSession<P>>();
	}
	
	public Map<Long, PlayerSession<P>> getPlayerSessions(){
		return Collections.unmodifiableMap(sessions);
	}
	
	public Map<Long, P> getPlayers(Set<Long> sessionIds){
		Map<Long, P> playerMap = new HashMap<Long, P>();
		for(Long sessionId: sessionIds){
			PlayerSession<P> session = sessions.get(sessionId);
			playerMap.put(sessionId, session.getPlayer());
		}
		return Collections.unmodifiableMap(playerMap);
	}

	public Map<Long, PlayerSession<P>> getPlayerSesssions(Set<Long> sessionIds){
		Map<Long, PlayerSession<P>> playerMap = new HashMap<Long, PlayerSession<P>>();
		for(Long sessionId: sessionIds){
			PlayerSession<P> session = sessions.get(sessionId);
			playerMap.put(sessionId, session);
		}
		return Collections.unmodifiableMap(playerMap);
	}
	
	public boolean put(long sessionId, PlayerSession<P> session) {
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

	public PlayerSession<P> getSession(long sessionId) {
		return sessions.get(sessionId);
	}

	public PlayerSession<P> createPlayerSession(P player, Channel channel) {
		PlayerSession<P> playerSession = new PlayerSession<P>();
		playerSession.setPlayer(player);
		playerSession.setAttrs(new HashMap<String, Object>());
		playerSession.setRoomId(0L);
		playerSession.setId(uniqueIdGeneratorService.generateSessionId());
		playerSession.setCreateTime(System.currentTimeMillis());
		playerSession.setChannel(channel);
		sessions.put(playerSession.getId(), playerSession);
		return playerSession;
	}

	public <T> T updatePlayerSession(long playerSessionId, SessionCallable<T, P> callable) {
		PlayerSession<P> playerSession = getSession(playerSessionId);
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
