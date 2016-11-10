package com.netease.pangu.game.meta;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.netease.pangu.game.common.meta.IAvatar;

@Document(collection="avatar")
public class Avatar implements IAvatar, Serializable {
	private static final long serialVersionUID = 1L;
	private ObjectId id;
	private String name;
	private String uuid;
	private long gameId;
	private long avatarId;
	private String avatarImg;
	private long writeToDbTime;
	private long lastLoginTime;
	private String server;
	@Override
	public String getName() {
		return this.name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getUuid() {
		return uuid;
	}
	@Override
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public long getWriteToDbTime() {
		return writeToDbTime;
	}
	public void setWriteToDbTime(long writeToDbTime) {
		this.writeToDbTime = writeToDbTime;
	}
	public long getLastLoginTime() {
		return lastLoginTime;
	}
	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	@Override
	public long getAvatarId() {
		return avatarId;
	}
	@Override
	public void setAvatarId(long id) {
		this.avatarId = id;
	}
	@Override
	public void setGameId(long gameId) {
		this.gameId = gameId;
	}
	@Override
	public long getGameId() {
		return this.gameId;
	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getAvatarImg() {
		return avatarImg;
	}
	public void setAvatarImg(String avatarImg) {
		this.avatarImg = avatarImg;
	}

}
