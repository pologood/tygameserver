package com.netease.pangu.game.service;

import com.netease.pangu.game.dao.CommonRedisDao;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * 
 * @author hzhuangcheng 
 * 
 */
@Service
public class GlobalValueCacheService {
	@Resource
	private CommonRedisDao commonRedisDao;
	private final static Logger logger = Logger.getLogger(GlobalValueCacheService.class);
	
	/**
	 * 通过MemCached是实现一个全局的乐观锁,保证同一时间只执行一个。 等待10秒如果超时，强行获取
 *         执行玩操作后，乐观锁会删除
	 * @param callback
	 * @return
	 */
	public <T> T doExclusively(CacheableValueGetter<T> callback) {
		String accessTokenLock = callback.getGlobalLockName();
		T cachedValue = callback.getCachedValue();
		if (cachedValue == null) {
			boolean lockAquired = false;
			for (int i = 0; i < 10; i++) { // 尝试10s
				lockAquired = commonRedisDao.setIfAbsent(accessTokenLock, "LOCKED");
				if (!lockAquired) {
					// 等待1s，下次再试
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				} else {
					// 成功获取了锁
					break;
				}
			}

			// 锁已经成功获取，或者超时强行获取
			if (!lockAquired) {
				// 锁超时了，强行获取
				commonRedisDao.putWithTTL(accessTokenLock, "LOCKED", 300, TimeUnit.SECONDS);
				logger.info(String.format("get lock %s", accessTokenLock));
			}
			try {
				cachedValue = callback.getCachedValue();
				if (cachedValue == null) {
					cachedValue = callback.getMissValue();
				}
			} finally {
				commonRedisDao.remove(accessTokenLock); // 删除锁
			}
		}

		return cachedValue;
	}

	/**
	 * @param cacheableValueWithTTLForUpdate
	 * @return
	 */
	public <T> T doCachedWithTTLForUpdate(CacheableValueWithTTLForUpdate<T> cacheableValueWithTTLForUpdate, long expireTime, TimeUnit timeUnit) {
		@SuppressWarnings("unchecked")
		T cachedValue = (T) commonRedisDao.get(cacheableValueWithTTLForUpdate
				.getCachedKey());

		String updateTimeKey = String.format("%s-udt",
				cacheableValueWithTTLForUpdate.getCachedKey());
		Long updateTime = (Long) commonRedisDao.get(updateTimeKey);
		if (updateTime == null) {
			updateTime = 0L;
		};

		boolean isExpired = System.currentTimeMillis() - updateTime < cacheableValueWithTTLForUpdate.getTTL() ? false : true;

		if (isExpired || cachedValue == null) {
			cachedValue = cacheableValueWithTTLForUpdate.getMissValue();
			if (cachedValue != null) {
				commonRedisDao.putWithTTL(
						cacheableValueWithTTLForUpdate.getCachedKey(),
						cachedValue, expireTime, timeUnit);
				commonRedisDao.put(updateTimeKey, System.currentTimeMillis());
			}
		}
		return cachedValue;
	}
	
	public <T> T doPersistentCachedWithTTLForUpdate(CacheableValueWithTTLForUpdate<T> cacheableValueWithTTLForUpdate) {
		@SuppressWarnings("unchecked")
		T cachedValue = (T) commonRedisDao.get(cacheableValueWithTTLForUpdate
				.getCachedKey());

		String updateTimeKey = String.format("%s-udt",
				cacheableValueWithTTLForUpdate.getCachedKey());
		Long updateTime = (Long) commonRedisDao.get(updateTimeKey);
		if (updateTime == null) {
			updateTime = 0L;
		};

		boolean isExpired = System.currentTimeMillis() - updateTime < cacheableValueWithTTLForUpdate.getTTL() ? false : true;

		if (isExpired || cachedValue == null) {
			cachedValue = cacheableValueWithTTLForUpdate.getMissValue();
			if (cachedValue != null) {
				commonRedisDao.put(
						cacheableValueWithTTLForUpdate.getCachedKey(),
						cachedValue);
				commonRedisDao.put(updateTimeKey, System.currentTimeMillis());
			}
		}
		return cachedValue;
	}
	
	
	/**
	 * 
	 * 需要定制更新时间key
	 * 需要自己处理缓存
	 * @param cacheableValueWithTTL
	 * @return
	 */
	public <T> T doCachedWithTTLForUpdate(CustomizeCacheableValueWithTTLForUpdate<T> cacheableValueWithTTL) {
		T cachedValue = null;
		try{
			cachedValue = cacheableValueWithTTL.getCachedValue();
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		
		Long updateTime = (Long) commonRedisDao.get(cacheableValueWithTTL.getUpdateTimeKey());
		if (updateTime == null) {
			updateTime = 0L;
		};

		boolean isExpired = System.currentTimeMillis() - updateTime < cacheableValueWithTTL.getTTL() ? false : true;

		if (isExpired || cachedValue == null) {
			cachedValue = cacheableValueWithTTL.getMissValue();
			if (cachedValue != null) {
				commonRedisDao.put(cacheableValueWithTTL.getUpdateTimeKey(), System.currentTimeMillis());
			}
		}
		return cachedValue;
	}
	
	@SuppressWarnings("unchecked")
	public <K, V> Map<K, V> doCachedWithTTL(BatchCacheableValueWithTTL<K, V> batchCacheableValueWithTTL, long expireTime, TimeUnit timeUnit){
		Map<K, V> resultMap = new HashMap<K, V>();
		Map<String, K> keyMap = new HashMap<String, K>();
		List<K> keys = batchCacheableValueWithTTL.getKeys();
		List<String> strKeys = new ArrayList<String>();
		for(K key: keys){
			String strKey = batchCacheableValueWithTTL.getCacheKey(key);
			strKeys.add(strKey);
			keyMap.put(strKey, key);
		}
		List<K> missedKeys = new ArrayList<K>();
		Map<String, Object> result = commonRedisDao.getWithKeys(strKeys);
		for(String key: result.keySet()){
			V val = (V)result.get(key);
			if(val != null){
				resultMap.put(keyMap.get(key), val);
			}else{
				missedKeys.add(keyMap.get(key));
			}
		}
		if(missedKeys.size() > 0){
			Map<K, V> missedValues = batchCacheableValueWithTTL.getMissValues(missedKeys);
			if(missedValues != null){
				for(K key: missedValues.keySet()){
					resultMap.put(key, missedValues.get(key));
					commonRedisDao.putWithTTL(batchCacheableValueWithTTL.getCacheKey(key), missedValues.get(key), expireTime, timeUnit);
				}
			}
		}
		return resultMap;	
	}

	public static interface CacheableValueWithTTLForUpdate<T> {
		public T getMissValue();
		public String getCachedKey();
		public long getTTL();
	}
	
	public static interface CustomizeCacheableValueWithTTLForUpdate<T> {
		public T getMissValue();
		public T getCachedValue();
		public String getUpdateTimeKey();
		public long getTTL();
	}
	
	public static interface BatchCacheableValueWithTTL<K, V> {
		public Map<K, V> getMissValues(List<K> keys);
		public List<K> getKeys();
		public String getCacheKey(K k);
	}

	public static interface CacheableValueGetter<T> {
		public T getMissValue();
		public T getCachedValue();
		public String getGlobalLockName();
	}
}
