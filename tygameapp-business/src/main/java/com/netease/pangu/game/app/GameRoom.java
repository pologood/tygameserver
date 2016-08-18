package com.netease.pangu.game.app;

import java.util.Set;

public class GameRoom {
	public static enum Status {
		IDLE, GAMEING
	}

	public static enum RoomType {
		PRIVATE, PUBLIC
	}

	private long id;
	private Set<Long> playerSessionIds;
	private long gameId;
	private Status status;
	private long ownerId;
	private int maxSize;
	private RoomType type;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Set<Long> getPlayerSessionIds() {
		return playerSessionIds;
	}

	public void setPlayerSessionIds(Set<Long> playerSessionIds) {
		this.playerSessionIds = playerSessionIds;
	}

	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public RoomType getType() {
		return type;
	}

	public void setType(RoomType type) {
		this.type = type;
	}

}
