package com.netease.pangu.game.app;

import java.util.Set;

public class GameRoom {
	public static enum Status {
		IDLE, GAMEING
	}
	
	private Set<PlayerSession> playerSessions;
	private Game game;
	
	public Set<PlayerSession> getPlayerSessions() {
		return playerSessions;
	}

	public void setPlayerSessions(Set<PlayerSession> playerSessions) {
		this.playerSessions = playerSessions;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}
	
	public synchronized boolean join(PlayerSession player){
		return playerSessions.add(player);
	}
	
	public synchronized boolean leave(PlayerSession player){
		return playerSessions.remove(player);
	}
	
}
