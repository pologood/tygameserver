package com.netease.pangu.game.dao.redis;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.util.Assert;

public abstract class RedisDao<K, V> {
	private RedisOperations<K, V> redisOperations;
	@Resource
	public AutowireCapableBeanFactory beanFactory;

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		RedisTemplateInject anno = getClass().getAnnotation(RedisTemplateInject.class);
		Assert.notNull(anno, "required RedisTemplate");
		Object bean = beanFactory.getBean(((RedisTemplateInject) anno).value(), RedisTemplate.class);
		Assert.notNull(bean);
		redisOperations = (RedisOperations<K, V>) bean;
	}
	
	
	public RedisOperations<K, V> getRedisOperations(){
		return redisOperations;
	}

	public void remove(K key) {
		redisOperations.delete(key);
	}

	public void put(K key, V value) {
		redisOperations.opsForValue().set(key, value);
	}

	public void put(K key, V value, long time, TimeUnit timeUnit) {
		redisOperations.opsForValue().set(key, value, time, timeUnit);
	}

	public void remove(List<K> keys) {
		redisOperations.delete(keys);
	}

	public boolean exist(K key) {
		return redisOperations.hasKey(key);
	}

	public boolean setIfAbsent(K key, V value) {
		return redisOperations.opsForValue().setIfAbsent(key, value);
	}

	public Object get(K key) {
		return redisOperations.opsForValue().get(key);
	}

	public Map<K, V> getWithKeys(List<K> keys) {
		List<V> values = getByKeys(keys);
		Iterator<K> kIt = keys.iterator();
		Iterator<V> vIt = values.iterator();
		Map<K, V> m = new LinkedHashMap<K, V>();
		while (kIt.hasNext() && vIt.hasNext()) {
			m.put(kIt.next(), vIt.next());
		}
		return m;
	}

	public List<V> getByKeys(List<K> keys) {
		return redisOperations.opsForValue().multiGet(keys);
	}

	public void putAll(Map<K, V> valueMap) {
		redisOperations.opsForValue().multiSet(valueMap);
	}

	// Set Operations
	@SuppressWarnings("unchecked")
	public Long addToSet(K boundedkey, V ... value) {
		return redisOperations.boundSetOps(boundedkey).add(value);
	}
	
	public long removeFromSet(K boundedkey, V value) {
		return redisOperations.boundSetOps(boundedkey).remove(value);
	}
	
	public Set<V> getSetMembers(K boundedKey) {
		return redisOperations.boundSetOps(boundedKey).members();
	}
	
	public Long getSetSize(K boundedKey) {
		return redisOperations.boundSetOps(boundedKey).size();
	}
	
	public V popFromSet(K boudedKey){
		return redisOperations.boundSetOps(boudedKey).pop();
	}
	
	// ZSet Operations
	public Long getRankInZSet(K boundedKey, V value) {
		return redisOperations.boundZSetOps(boundedKey).rank(value);
	}

	public Long getReverseRankInZSet(K boundedKey, V value) {
		return redisOperations.boundZSetOps(boundedKey).reverseRank(value);
	}

	public long removeFromZSet(K boundedkey, V value) {
		return redisOperations.boundZSetOps(boundedkey).remove(value);
	}

	public Long addAllToZSet(K boundedkey, Set<TypedTuple<V>> set) {
		return redisOperations.boundZSetOps(boundedkey).add(set);
	}

	public Boolean addToZSet(K boundedkey, V value, double score) {
		return redisOperations.boundZSetOps(boundedkey).add(value, score);
	}

	public long getZSetSize(K boundedKey) {
		return redisOperations.boundZSetOps(boundedKey).count(0, Double.MAX_VALUE);
	}

	public Set<V> getZSetValuesByRankRange(K boundedKey, long minRank, long maxRank) {
		return redisOperations.boundZSetOps(boundedKey).range(minRank, maxRank);
	}

	public Set<V> getZSetValuesByReverseRankRange(K boundedKey, long minRank, long maxRank) {
		return redisOperations.boundZSetOps(boundedKey).reverseRange(minRank, maxRank);
	}

	public Set<TypedTuple<V>> getZSetValuesWithScoreByRankRange(K boundedKey, long minRank, long maxRank) {
		return redisOperations.boundZSetOps(boundedKey).rangeWithScores(minRank, maxRank);
	}

	public Set<TypedTuple<V>> getZSetValuesWithScoreByScoreRange(K boundedKey, double minScore, double maxScore) {
		return redisOperations.boundZSetOps(boundedKey).rangeByScoreWithScores(minScore, maxScore);
	}

	public Set<TypedTuple<V>> getZSetValuesWithScoreByReverseScoreRange(K boundedKey, double minScore,
			double maxScore) {
		return redisOperations.boundZSetOps(boundedKey).reverseRangeByScoreWithScores(minScore, maxScore);
	}

	public Set<V> getZSetValuesByReverseScoreRange(K boundedKey, double minScore, double maxScore) {
		return redisOperations.boundZSetOps(boundedKey).reverseRangeByScore(minScore, maxScore);
	}

	public Set<V> getZSetValuesByScoreRange(K boundedKey, double minScore, double maxScore) {
		return redisOperations.boundZSetOps(boundedKey).rangeByScore(minScore, maxScore);
	}
	
	// Hash Operations
	public <HK, HV> List<HV> getHashValues(K boundedKey, Collection<HK> keys) {
		return redisOperations.<HK, HV> boundHashOps(boundedKey).multiGet(keys);
	}

	public <HK, HV> HV get(K boundedKey, HK keyInHash) {
		return redisOperations.<HK, HV> boundHashOps(boundedKey).get(keyInHash);
	}
	
	public <HK, HV> boolean putIfAbsent(K boundedKey, HK keyInHash, HV value) {
		return redisOperations.<HK, HV> boundHashOps(boundedKey).putIfAbsent(keyInHash, value);
	}
	
	public <HK, HV> void put(K boundedKey, HK keyInHash, HV value) {
		redisOperations.<HK, HV> boundHashOps(boundedKey).put(keyInHash, value);
	}
	
	public <HK, HV> void delete(K boundedKey, HK keyInHash) {
		redisOperations.<HK, HV> boundHashOps(boundedKey).delete(keyInHash);
	}
	
	public long getHashSize(K boundedKey) {
		return redisOperations.boundHashOps(boundedKey).size();
	}
	
	// List Operations
	@SuppressWarnings("unchecked")
	public Long rightPushAll(K boundedKey, V... values){
		return redisOperations.boundListOps(boundedKey).rightPushAll(values);
	}
	
	public Long getListSize(K boundedKey){
		return redisOperations.boundListOps(boundedKey).size();
	}
	
	public Long rightPush(K boundedKey, V value){
		return redisOperations.boundListOps(boundedKey).rightPush(value);
	}

	public V leftPop(K boundedKey){
		return redisOperations.boundListOps(boundedKey).leftPop();
	}
	
	

}
