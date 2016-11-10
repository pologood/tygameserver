package com.netease.pangu.game.common.meta;

import java.util.Map;

import com.netease.pangu.game.util.JsonUtil;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class AvatarSession<A extends IAvatar> implements IAvatar{
	private final static int WAIT_MILLIS = 5 * 1000;

	public static enum Status {
		NOT_CONNECTED, CONNECTING, CONNECTED, CLOSED
	}
	private A avatar;
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
			channel.close().awaitUninterruptibly(WAIT_MILLIS);
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
	public A getAvatar() {
		return avatar;
	}
	public void setAvatar(A avatar) {
		this.avatar = avatar;
	}
	@Override
	public long getAvatarId() {
		return avatar.getAvatarId();
	}
	@Override
	public void setAvatarId(long id) {
		throw new UnsupportedOperationException("method not support");
	}
	@Override
	public String getName() {
		return avatar.getName();
	}
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("method not support");
	}
	@Override
	public String getUuid() {
		return avatar.getUuid();
	}
	@Override
	public void setUuid(String uuid) {
		throw new UnsupportedOperationException("method not support");
	}
	@Override
	public String getServer() {
		return avatar.getServer();
	}
	@Override
	public void setServer(String server) {
		throw new UnsupportedOperationException("method not support");
		
	}
	@Override
	public void setGameId(long gameId) {
		throw new UnsupportedOperationException("method not support");
	}
	@Override
	public long getGameId() {
		return avatar.getGameId();
	}

}
