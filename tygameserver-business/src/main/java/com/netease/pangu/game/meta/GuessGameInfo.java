package com.netease.pangu.game.meta;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by huangc on 2017/2/8.
 */
@Document(collection = "guessgame_info")
public class GuessGameInfo {
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    @Field("_id")
    private ObjectId id;
}
