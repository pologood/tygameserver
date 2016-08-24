package com.netease.pangu.game.meta;

import java.util.Map;

import io.netty.channel.Channel;

public class PlayerSession {
	private final static int WATI_MILLIS = 5 * 1000;

	public static enum Status {
		NOT_CONNECTED, CONNECTING, CONNECTED, CLOSED
	}

	private long playerId;
	private long roomId;
	private Channel channel;
	private Map<String, Object> attrs;
	private long createTime;
	private long lastestActiveTime;
	public Map<String, Object> getAttrs() {
		return attrs;
	}

	public void setAttrs(Map<String, Object> attrs) {
		this.attrs = attrs;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

	public void close() {
		if (channel != null) {
			channel.flush();
			channel.close().awaitUninterruptibly(WATI_MILLIS);
		}
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getLastestActiveTime() {
		return lastestActiveTime;
	}

	public void setLastestActiveTime(long lastestActiveTime) {
		this.lastestActiveTime = lastestActiveTime;
	}

}
