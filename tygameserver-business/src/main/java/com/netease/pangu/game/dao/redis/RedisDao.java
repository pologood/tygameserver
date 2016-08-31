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
	public RedisOperations<K, V> redisOperations;

	@Resource
	public AutowireCapableBeanFactory beanFactory;
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init(){
		RedisTemplateInject anno = getClass().getAnnotation(RedisTemplateInject.class);
		Assert.notNull(anno, "required RedisTemplate");
		Object bean = beanFactory.getBean(((RedisTemplateInject)anno).value(), RedisTemplate.class);
		Assert.notNull(bean);
		redisOperations = (RedisOperations<K, V>) bean;
	}

	public RedisOperations<K, V> getRedisOperations() {
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

	public boolean setIfAbsent(K key, V value) {
		return redisOperations.opsForValue().setIfAbsent(key, value);
	}

	public Object get(K key) {
		return redisOperations.opsForValue().get(key);
	}
	
	public boolean exist(K key){
		return redisOperations.hasKey(key);
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

	/**
	 * ��ȡָ��boundedKey��zset�У�ָ��Ԫ�ص�����
	 * 
	 * @param boundedKey
	 * @return
	 */
	public Long getRankInZSet(K boundedKey, V value) {
		return redisOperations.boundZSetOps(boundedKey).rank(value);
	}

	/**
	 * ��ȡָ��boundedKey��zset�У�ָ��Ԫ�صĵ�������
	 * 
	 * @param boundedKey
	 * @return
	 */
	public Long getReverseRankInZSet(K boundedKey, V value) {
		return redisOperations.boundZSetOps(boundedKey).reverseRank(value);
	}

	/**
	 * ɾ��zset��ָ����Ԫ�أ�����ʵ��ɾ����Ԫ����Ŀ
	 * 
	 * @param boundedkey
	 * @param key
	 * @return
	 */
	public long removeFromZSet(K boundedkey, V value) {
		return redisOperations.boundZSetOps(boundedkey).remove(value);
	}

	public Long addAllToZSet(K boundedkey, Set<TypedTuple<V>> set) {
		return redisOperations.boundZSetOps(boundedkey).add(set);
	}

	public Boolean addToZSet(K boundedkey, V value, double score) {
		return redisOperations.boundZSetOps(boundedkey).add(value, score);
	}

	@SuppressWarnings("unchecked")
	public Long addToSet(K boundedkey, V value) {
		return redisOperations.boundSetOps(boundedkey).add(value);
	}

	/**
	 * ɾ��set��ָ����Ԫ�أ�����ʵ��ɾ����Ԫ����Ŀ
	 * 
	 * @param boundedkey
	 * @param key
	 * @return
	 */
	public long removeFromSet(K boundedkey, V value) {
		return redisOperations.boundSetOps(boundedkey).remove(value);
	}

	public long getZSetSize(K boundedKey) {
		return redisOperations.boundZSetOps(boundedKey).count(0, Double.MAX_VALUE);
	}

	/**
	 * ��ȡָ��boundedKey��zset�У����շ���˳��ģ�ָ��<b>����</b>��Χ�ڵ�Ԫ��
	 * 
	 * @param boundedKey
	 * @param min
	 * @param max
	 * @return
	 */
	public Set<V> getZSetValuesByRankRange(K boundedKey, long minRank, long maxRank) {
		return redisOperations.boundZSetOps(boundedKey).range(minRank, maxRank);
	}

	/**
	 * ��ȡָ��boundedKey��zset�У����շ�������ģ�ָ��<b>����</b>��Χ�ڵ�Ԫ��
	 * 
	 * @param boundedKey
	 * @param min
	 * @param max
	 * @return
	 */
	public Set<V> getZSetValuesByReverseRankRange(K boundedKey, long minRank, long maxRank) {
		return redisOperations.boundZSetOps(boundedKey).reverseRange(minRank, maxRank);
	}

	/**
	 * ��ȡָ��boundedKey��zset�У����շ���˳��ģ�ָ��<b>����</b>��Χ�ڵ�Ԫ���Լ�����
	 * 
	 * @param boundedKey
	 * @param min
	 * @param max
	 * @return
	 */
	public Set<TypedTuple<V>> getZSetValuesWithScoreByRankRange(K boundedKey, long minRank, long maxRank) {
		return redisOperations.boundZSetOps(boundedKey).rangeWithScores(minRank, maxRank);
	}

	/**
	 * ��ȡָ��boundedKey��zset�У����շ���˳��ģ�ָ��<b>����</b>��Χ�ڵ�Ԫ���Լ�����
	 * 
	 * @param boundedKey
	 * @param min
	 * @param max
	 * @return
	 */
	public Set<TypedTuple<V>> getZSetValuesWithScoreByScoreRange(K boundedKey, double minScore, double maxScore) {
		return redisOperations.boundZSetOps(boundedKey).rangeByScoreWithScores(minScore, maxScore);
	}

	/**
	 * ��ȡָ��boundedKey��zset�У����շ�������ģ�ָ��<b>����</b>��Χ�ڵ�Ԫ���Լ�����
	 * 
	 * @param boundedKey
	 * @param min
	 * @param max
	 * @return
	 */
	public Set<TypedTuple<V>> getZSetValuesWithScoreByReverseScoreRange(K boundedKey, double minScore,
			double maxScore) {
		return redisOperations.boundZSetOps(boundedKey).reverseRangeByScoreWithScores(minScore, maxScore);
	}

	/**
	 * ��ȡָ��boundedKey��zset�У����շ�������ģ�ָ��<b>����</b>��Χ�ڵ�Ԫ��
	 * 
	 * @param boundedKey
	 * @param min
	 * @param max
	 * @return
	 */
	public Set<V> getZSetValuesByReverseScoreRange(K boundedKey, double minScore, double maxScore) {
		return redisOperations.boundZSetOps(boundedKey).reverseRangeByScore(minScore, maxScore);
	}

	/**
	 * ��ȡָ��boundedKey��zset�У����շ���˳��ģ�ָ��<b>����</b>��Χ�ڵ�Ԫ��
	 * 
	 * @param boundedKey
	 * @param min
	 * @param max
	 * @return
	 */
	public Set<V> getZSetValuesByScoreRange(K boundedKey, double minScore, double maxScore) {
		return redisOperations.boundZSetOps(boundedKey).rangeByScore(minScore, maxScore);
	}

	public <HK, HV> List<HV> getHashValues(K boundedKey, Collection<HK> keys) {
		return redisOperations.<HK, HV> boundHashOps(boundedKey).multiGet(keys);
	}

	public <HK, HV> HV getHashValue(K boundedKey, HK keyInHash) {
		return redisOperations.<HK, HV> boundHashOps(boundedKey).get(keyInHash);
	}

	public Set<V> getSetMembers(K boundedKey) {
		return redisOperations.boundSetOps(boundedKey).members();
	}

	public long getHashSize(K boundedKey) {
		return redisOperations.boundHashOps(boundedKey).size();
	}

}
