package com.netease.pangu.game.common.meta;

import java.util.Map;

import com.netease.pangu.game.util.JsonUtil;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class PlayerSession<P extends IPlayer> {
	private final static int WATI_MILLIS = 5 * 1000;

	public static enum Status {
		NOT_CONNECTED, CONNECTING, CONNECTED, CLOSED
	}
	
	private long id;
	private P player;
	private long roomId;
	private Channel channel;
	private Map<String, Object> attrs;
	private long createTime;
	private long lastestActiveTime;
	public Map<String, Object> getAttrs() {
		return attrs;
	}
	public void sendMessage(Object msg){
		if (channel != null) {
			channel.writeAndFlush(msg);
		}
	}
	
	public void sendJSONMessage(Object msg){
		if (channel != null) {
			channel.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(msg)));
		}
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
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public P getPlayer() {
		return player;
	}
	public void setPlayer(P player) {
		this.player = player;
	}

}
