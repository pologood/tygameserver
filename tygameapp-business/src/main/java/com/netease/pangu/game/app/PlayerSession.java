package com.netease.pangu.game.app;

import io.netty.channel.Channel;

public class PlayerSession {
	private final static int WATI_MILLIS = 5*1000;
	public static enum Status
	{
		NOT_CONNECTED, CONNECTING, CONNECTED, CLOSED
	}
	
	private final Object playerId;
	private Object roomId;
	private Channel sender;
	
	public PlayerSession(Object playerId){
		this.playerId = playerId;
	}

	public Object getPlayerId() {
		return playerId;
	}
	
	public Channel getSender() {
		return sender;
	}

	public void setSender(Channel sender) {
		this.sender = sender;
	}

	public Object getRoomId() {
		return roomId;
	}

	public void setRoomId(Object roomId) {
		this.roomId = roomId;
	}
	
	public void close(){
		if(sender != null){
			sender.flush();
			sender.close().awaitUninterruptibly(WATI_MILLIS);
		}
	}

}
