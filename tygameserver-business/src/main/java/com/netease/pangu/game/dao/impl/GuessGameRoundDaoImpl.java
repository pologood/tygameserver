package com.netease.pangu.game.dao.impl;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.netease.pangu.game.dao.AbstractMongoDao;
import com.netease.pangu.game.meta.GuessGameRound;

@Component
public class GuessGameRoundDaoImpl extends AbstractMongoDao<GuessGameRound> {
	public List<GuessGameRound> getAvatarGuessGameByAvatarId(long gameId, long avatarId, long roomId) {
		Criteria criteria = Criteria.where("drawerId").is(avatarId);
		criteria.andOperator(Criteria.where("gameId").is(gameId));
		criteria.andOperator(Criteria.where("roomId").is(roomId));
		Query query = new Query(criteria);
		return this.find(query, GuessGameRound.class);
	}
}
