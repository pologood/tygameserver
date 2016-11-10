package com.netease.pangu.game.common.meta;

public interface IAvatar {
	public long getAvatarId();
	public void setAvatarId(long id);
	public String getUuid();
	public void setUuid(String uuid);
	public String getName();
	public void setName(String name);
	public String getServer();
	public void setServer(String server);
	public void setGameId(long gameId);
	public long getGameId();
}
