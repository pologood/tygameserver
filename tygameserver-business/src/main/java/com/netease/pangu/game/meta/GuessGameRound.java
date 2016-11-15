package com.netease.pangu.game.meta;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="guessGameRound")
public class GuessGameRound {
	private long gameId;
	private long roomId;
	private long startTime;
	private long endTime;
	private List<Long> avatarIds;
	private long drawerId;
	private Map<String, Object> result;
	public long getRoomId() {
		return roomId;
	}
	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public List<Long> getAvatarIds() {
		return avatarIds;
	}
	public void setAvatarIds(List<Long> avatarIds) {
		this.avatarIds = avatarIds;
	}
	public long getDrawerId() {
		return drawerId;
	}
	public void setDrawerId(long drawerId) {
		this.drawerId = drawerId;
	}
	public Map<String, Object> getResult() {
		return result;
	}
	public void setResult(Map<String, Object> result) {
		this.result = result;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public long getGameId() {
		return gameId;
	}
	public void setGameId(long gameId) {
		this.gameId = gameId;
	}
	
}
