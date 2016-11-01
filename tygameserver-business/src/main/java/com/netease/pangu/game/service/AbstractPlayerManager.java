package com.netease.pangu.game.service;

import javax.annotation.Resource;

import com.netease.pangu.game.common.meta.IPlayer;

public abstract class AbstractPlayerManager<P extends IPlayer> {
	@Resource
	private UniqueIDGeneratorService uniqueIdGeneratorService;
	
	public AbstractPlayerManager(){
	}
	/**
	 * 
	 * @param player
	 * @return 如果角色已存在返回存在的角色
	 */
	public P createPlayer(P player){
		long playerId = uniqueIdGeneratorService.generatePlayerId();
		player.setPid(playerId);
		return put(playerId, player);
	}
	
	protected abstract P put(long playerId, P player); 
	
	
	protected abstract P get(long playerId);
}
