package com.netease.pangu.game.dao.impl;

import com.netease.pangu.game.dao.mongo.AbstractMongoDao;
import com.netease.pangu.game.meta.GuessGameInfo;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * Created by huangc on 2017/1/19.
 */
@Component
public class GuessGameInfoDaoImpl extends AbstractMongoDao<GuessGameInfo> {

    public GuessGameInfo getGuessGameById(ObjectId id){
        Criteria criteria = Criteria.where("_id").is(id);
        Query query = new Query(criteria);
        return super.findOne(query, GuessGameInfo.class);
    }

    public boolean insertGuessGameInfo(GuessGameInfo game) {
        try {
            this.insert(game);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean saveGuessGameInfo(GuessGameInfo game) {
        try {
           return this.save(game);
        } catch (Exception e) {
            return false;
        }
    }
}
