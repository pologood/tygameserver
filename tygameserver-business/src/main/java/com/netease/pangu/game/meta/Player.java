package com.netease.pangu.game.meta;

import java.io.Serializable;

import com.netease.pangu.game.common.meta.IPlayer;

public class Player implements IPlayer, Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private long id;
	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return id;
	}
	@Override
	public void setId(long id) {
		this.id = id;
		
	}
	@Override
	public String getName() {
		return this.name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}

}
