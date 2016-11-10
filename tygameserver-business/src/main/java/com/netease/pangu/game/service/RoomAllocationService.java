package com.netease.pangu.game.service;

import java.util.ArrayList;
import java.util.List;

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

	public Long borrowRoom(long gameId){
		String key = getKey(gameId, ROOMS_AVAILABLE);
		if(commonRedisDao.getListSize(key) == 0){
			allocateRooms(gameId, defaultCapacity);
		}
		Long room = (Long)commonRedisDao.leftPop(key);
		return room;
	}
	
	public boolean returnRoom(long gameId, Long room){
		String key = getKey(gameId, ROOMS_AVAILABLE);
		return commonRedisDao.rightPush(key, room) > 0;
	}
}
