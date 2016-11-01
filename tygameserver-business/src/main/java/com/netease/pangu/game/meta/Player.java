package com.netease.pangu.game.meta;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;

import com.netease.pangu.game.common.meta.IPlayer;

@Document(collection="player")
public class Player implements IPlayer, Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private long pId;
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
		// TODO Auto-generated method stub
		return pId;
	}
	@Override
	public void setPid(long id) {
		pId = id;
	}

}
