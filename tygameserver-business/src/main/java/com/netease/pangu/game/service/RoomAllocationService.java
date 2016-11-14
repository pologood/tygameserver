package com.netease.pangu.game.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import com.netease.pangu.game.dao.CommonRedisDao;

@Service
public class RoomAllocationService {
	@Resource private CommonRedisDao commonRedisDao;
	private final static String ROOMS_AVAILABLE = "rooms_available";
	private final static String ROOMS_CAPACITY = "rooms_capacity";
	private final static String ROOMS_INFO = "rooms_info";
	public static String getKey(long gameId, String key){
		return String.format("%d-%s", gameId, key);
	}
	
	private final static int defaultCapacity = 1000;
	
	public long getRoomCapacity(long gameId){
		Long capacity = (Long)commonRedisDao.get(getKey(gameId, ROOMS_CAPACITY));
		if(capacity == null){
			capacity = 0L;
		}
		return capacity;
	}
	
	public String getRoomInfo(long gameId, long roomId){
		return commonRedisDao.get(getKey(gameId, ROOMS_INFO), roomId);
	}
	
	public void allocateRooms(long gameId, int num){
		long capacity = getRoomCapacity(gameId);
		final List<Long> roomIds = new ArrayList<Long>();
		for(long i = capacity + 1; i <= capacity + num; i ++){
			roomIds.add(i);
		}
		final String availableKey = getKey(gameId ,ROOMS_AVAILABLE);
		final String capacityKey = getKey(gameId, ROOMS_CAPACITY);
		final long roomCapacity = capacity + num;
		commonRedisDao.getRedisOperations().execute(new SessionCallback<Boolean>() {
			@SuppressWarnings("unchecked")
			@Override
			public Boolean execute(@SuppressWarnings("rawtypes") RedisOperations operations) throws DataAccessException {
				operations.boundSetOps(availableKey).add(roomIds.toArray());
				operations.opsForValue().set(capacityKey, roomCapacity);
				return null;
			}
		});
	}

	public Long borrowRoom(long gameId, final String server){
		final String availableKey = getKey(gameId, ROOMS_AVAILABLE);
		final String roomInfoKey = getKey(gameId, ROOMS_INFO);
		if(commonRedisDao.getSetSize(availableKey) == 0){
			allocateRooms(gameId, defaultCapacity);
		}
		return commonRedisDao.getRedisOperations().execute(new SessionCallback<Long>() {
			@SuppressWarnings("unchecked")
			@Override
			public Long execute(@SuppressWarnings("rawtypes") RedisOperations operations) throws DataAccessException {
				Long roomId = (Long)operations.boundSetOps(availableKey).pop();
				if(roomId != null){
					if(operations.boundHashOps(roomInfoKey).putIfAbsent(roomId, server)){
						return roomId;
					}
				}
				return null;
			}
		});
	}
	
	public boolean returnRoom(long gameId, final long roomId){
		final String availableKey = getKey(gameId, ROOMS_AVAILABLE);
		final String roomInfoKey = getKey(gameId, ROOMS_INFO);
		return commonRedisDao.getRedisOperations().execute(new SessionCallback<Boolean>() {
			@SuppressWarnings("unchecked")
			@Override
			public Boolean execute(@SuppressWarnings("rawtypes") RedisOperations operations) throws DataAccessException {
				Long count = (Long)operations.boundSetOps(availableKey).add(roomId);
				if(count > 0){
					operations.boundHashOps(roomInfoKey).delete(roomId);
					return true;
				}
				return false;
			}
		});
	}
}
