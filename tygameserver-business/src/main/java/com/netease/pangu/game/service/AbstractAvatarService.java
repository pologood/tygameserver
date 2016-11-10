package com.netease.pangu.game.service;

import javax.annotation.Resource;

import com.netease.pangu.game.common.meta.IAvatar;

public abstract class AbstractAvatarService<A extends IAvatar> {
	@Resource
	private UniqueIDGeneratorService uniqueIdGeneratorService;
	
	public AbstractAvatarService(){
	}
	/**
	 * 
	 * @param player
	 * @return 如果角色已存在返回存在的角色
	 */
	public A createAvatar(A avatar){
		long playerId = uniqueIdGeneratorService.generateAvatarId();
		avatar.setAvatarId(playerId);
		return put(playerId, avatar);
	}
	
	protected abstract A put(long avatarId, A avatar); 
	
	
	protected abstract A get(long avatarId);
}
