package com.netease.pangu.game.dao.impl;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.mongodb.WriteResult;
import com.netease.pangu.game.dao.AvatarDao;
import com.netease.pangu.game.dao.mongo.AbstractMongoDao;
import com.netease.pangu.game.meta.Avatar;

@Component
public class AvatarDaoImpl extends AbstractMongoDao<Avatar> implements AvatarDao<Avatar> {
	@Override
	public Avatar getAvatarByAvatarId(long id){
		Query query = new Query(Criteria.where("avatarId").is(id));
		return this.findOne(query, Avatar.class);
	}
	
	@Override
	public Avatar getAvatarByUUID(long gameId, String uuid){
		Criteria criteria = Criteria.where("uuid").is(uuid);
		criteria.andOperator(Criteria.where("gameId").is(gameId));
		Query query = new Query(criteria);
		return this.findOne(query, Avatar.class);
	}
	
	public List<Avatar> getListByGameId(long gameId){
		Criteria criteria = Criteria.where("gameId").is(gameId);
		Query query = new Query(criteria);
		return this.find(query, Avatar.class);
	}

	public boolean insertAvatar(Avatar avatar) {
		try{
			this.insert(avatar);
		}catch(Exception e){
			return false;
		}
		return true;
	}

	@Override
	public boolean removeAvatar(Avatar avatar) {
		WriteResult result = this.remove(avatar);
		return result.getN() > 0;
	}
	
	public boolean removeAvatarByAvatarId(Avatar avatar) {
		Query query = new Query(Criteria.where("avatarId").is(avatar.getAvatarId()));
		WriteResult result = this.getMongoTemplate().remove(query, Avatar.class);
		return result.getN() > 0;
	}
}
