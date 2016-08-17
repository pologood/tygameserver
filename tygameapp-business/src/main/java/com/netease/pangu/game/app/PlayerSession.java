package com.netease.pangu.game.app;

public class PlayerSession {
	public static enum Status
	{
		NOT_CONNECTED, CONNECTING, CONNECTED, CLOSED
	}
	
	private Object id;
	private String name;
	private GameRoom gameRoom;
	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GameRoom getGameRoom() {
		return gameRoom;
	}

	public void setGameRoom(GameRoom gameRoom) {
		this.gameRoom = gameRoom;
	}
}
