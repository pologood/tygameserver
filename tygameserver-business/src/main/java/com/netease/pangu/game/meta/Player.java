package com.netease.pangu.game.meta;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import com.netease.pangu.game.common.meta.IPlayer;

@Document(collection="player")
public class Player implements IPlayer, Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String uuid;
	private long pId;
	private long writeToDbTime;
	private long lastLoginTime;
	private Map<String, Object> serverAttrs;
	@Override
	public String getName() {
		return this.name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public long getPid() {
		return pId;
	}
	@Override
	public void setPid(long id) {
		pId = id;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public Map<String, Object> getServerAttrs() {
		return serverAttrs;
	}
	public void setServerAttrs(Map<String, Object> serverAttrs) {
		this.serverAttrs = serverAttrs;
	}
	public long getpId() {
		return pId;
	}
	public void setpId(long pId) {
		this.pId = pId;
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

}
