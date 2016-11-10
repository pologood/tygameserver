package com.netease.pangu.game.dao;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.WriteResult;

public class AbstractMongoDao<T> {
	@Resource private MongoTemplate mongoTemplate;
	private T nullIfEmpty(List<T> list) {
		return list == null || list.isEmpty() ? null : list.get(0);
	}
	
	public String getCollectionName(Class<T> clazz){
		return mongoTemplate.getCollectionName(clazz);
	}

	public T findOne(Query query, Class<T> clazz) {
		return nullIfEmpty(mongoTemplate.find(query, clazz));
	}
	
	public List<T> find(Query query, Class<T> clazz) {
		return mongoTemplate.find(query, clazz);
	}
	
	public WriteResult update(Query query, Update update, Class<T> clazz){
		return mongoTemplate.updateFirst(query, update, clazz);
	}
	
	public void insert(T object){
		mongoTemplate.insert(object);
	}
	
	public void save(T object){
		mongoTemplate.save(object);
	}
	
	public void insertAll(List<T> list){
		mongoTemplate.insertAll(list);
	}
	
	public WriteResult remove(T object){
		return mongoTemplate.remove(object);
	}

	public MongoTemplate getMongoTemplate() {
		return mongoTemplate;
	}
	
}
