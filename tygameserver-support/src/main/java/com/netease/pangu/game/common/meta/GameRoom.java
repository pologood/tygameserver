package com.netease.pangu.game.common.meta;

import java.util.Map;

public class GameRoom {
	public static enum RoomType {
		PRIVATE, PUBLIC
	}

	private long id;
	private Map<Integer, Long> sessionIds;
	private long gameId;
	private RoomStatus status;
	private long ownerId;
	private int maxSize;
	private RoomType type;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	public RoomStatus getStatus() {
		return status;
	}

	public void setStatus(RoomStatus status) {
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

	public Map<Integer, Long> getSessionIds() {
		return sessionIds;
	}

	public void setSessionIds(Map<Integer, Long> sessionIds) {
		this.sessionIds = sessionIds;
	}

}
