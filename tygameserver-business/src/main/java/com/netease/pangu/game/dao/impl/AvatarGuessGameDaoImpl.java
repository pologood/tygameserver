package com.netease.pangu.game.dao.impl;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.netease.pangu.game.dao.AbstractMongoDao;
import com.netease.pangu.game.meta.AvatarGuessGame;

@Component
public class AvatarGuessGameDaoImpl extends AbstractMongoDao<AvatarGuessGame> {
	public AvatarGuessGame getAvatarGuessGameByAvatarId(long gameId, long avatarId) {
		Criteria criteria = Criteria.where("avatarId").is(avatarId);
		criteria.andOperator(Criteria.where("gameId").is(gameId));
		Query query = new Query(criteria);
		return this.findOne(query, AvatarGuessGame.class);
	}
}
