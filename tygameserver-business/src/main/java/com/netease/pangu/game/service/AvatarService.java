package com.netease.pangu.game.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.netease.pangu.game.dao.impl.AvatarDaoImpl;
import com.netease.pangu.game.meta.Avatar;

@Component
public class AvatarService extends AbstractAvatarService<Avatar> {
	@Resource private AvatarDaoImpl avatarDao;
	
	@Override
	protected Avatar put(long avatarId, Avatar avatar) {
		Avatar p = avatarDao.getAvatarByAvatarId(avatarId);
		if(p == null){
			if(avatarDao.insertAvatar(avatar)){
				p = avatarDao.getAvatarByAvatarId(avatarId);
			}
		}
		return p;
	}
	
	public void logOut(Avatar avatar){
		avatar.setWriteToDbTime(System.currentTimeMillis());
		avatar.setServer("");
		avatarDao.save(avatar);
	}
	
	public List<Avatar> getListByGameId(long gameId){
		return avatarDao.getListByGameId(gameId);
	}
	
	public Avatar getAvatarByAvatarId(long avatarId) {
		return avatarDao.getAvatarByAvatarId(avatarId);
	}

	public Avatar getAvatarByUUID(long gameId, String uuid) {
		return avatarDao.getAvatarByUUID(gameId, uuid);
	}

	public boolean insert(Avatar avatar) {
		return avatarDao.insertAvatar(avatar);
	}

	public boolean save(Avatar avatar) {
		return avatarDao.save(avatar);
	}

	public boolean remove(Avatar avatar) {
		return avatarDao.removeAvatar(avatar);
	}

	@Override
	protected Avatar get(long avatarId) {
		return avatarDao.getAvatarByAvatarId(avatarId);
	}
	
}
