package com.netease.pangu.game.dao;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class UniqueIDGenerateDao{
	@Resource private MongoTemplate mongoTemplate;
	private static String collectionName = "game_unique";
		
	public void getAndSetInitValue(String key, long initValue){
		Query query = new Query(Criteria.where("key").is(key));
		@SuppressWarnings("rawtypes")
		Map obj = mongoTemplate.findOne(query, Map.class, collectionName);
		if(obj == null){
			Map<String, Object> objectToSave = new HashMap<String, Object>();
			objectToSave.put("key", key);
			objectToSave.put("value", initValue);
			mongoTemplate.insert(objectToSave, collectionName);
		}
		
	}
	
	public Long generate(String key){
		Query query = new Query(Criteria.where("key").is(key));
		Update update = new Update().inc("value", 1);
		return (Long)mongoTemplate.findAndModify(query, update, Map.class, collectionName).get("value");		
	}
	
}
