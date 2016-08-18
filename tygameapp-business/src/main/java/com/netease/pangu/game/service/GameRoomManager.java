package com.netease.pangu.game.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.netease.pangu.game.app.GameRoom;
import com.netease.pangu.game.app.GameRoom.Status;
import com.netease.pangu.game.app.PlayerSession;

@Component
public class GameRoomManager {
	@Resource
	private PlayerSessionManager playerSessionManager;
	@Resource
	private UniqueIDGeneratorService uniqueIdGeneratorService;
	
	private final ConcurrentMap<Object, GameRoom> rooms = new ConcurrentHashMap<Object, GameRoom>();
	private final ConcurrentMap<Object, Set<GameRoom>> gameIdRefRoom = new ConcurrentHashMap<Object, Set<GameRoom>>();
	private ConcurrentMap<Long, Object> lockMap = new ConcurrentHashMap<Long, Object>();
	
	private Object getLock(long playerSessionId) {
		lockMap.putIfAbsent(playerSessionId, new Object());
		return lockMap.get(playerSessionId);
	}
	
	public boolean remove(Object roomId){
		if(roomId == null){
			return false;
		}
		GameRoom room = rooms.remove(roomId);
		if(room != null){
			Set<GameRoom> refRooms = gameIdRefRoom.get(room.getGameId());
			if(refRooms != null){
				gameIdRefRoom.get(room.getGameId()).remove(room);
			}
			return true;
		}
		return false;
	}
	
	public GameRoom getGameRoom(Object key){
		return rooms.get(key);
	}
	

	/**
	 * 
	 * @param gameId
	 * @param playerSessionId
	 * @param type
	 * @return roomId
	 */
	public long createRoom(long gameId, long playerSessionId) {
		PlayerSession playerSession = playerSessionManager.get(playerSessionId);
		synchronized (getLock(playerSessionId)) {
			if (playerSession != null && playerSession.getRoomId() == null) {
				long roomId = uniqueIdGeneratorService.generate();
				GameRoom room = new GameRoom();
				room.setId(roomId);
				room.setGameId(gameId);
				room.setOwnerId(playerSessionId);
				room.setPlayerSessionIds(new HashSet<Long>());
				room.setStatus(Status.IDLE);
				
				rooms.put(roomId, room);
				gameIdRefRoom.putIfAbsent(gameId, new HashSet<GameRoom>());
				gameIdRefRoom.get(gameId).add(room);
				return roomId;
			}
		}
		return -1L;
	}

	public boolean joinRoom(Object playerSessionId, Object roomId) {
		PlayerSession playerSession = playerSessionManager.get(playerSessionId);
		GameRoom room = getGameRoom(roomId);
		if (playerSession != null && playerSession.getRoomId() == null) {
			if(room != null && room.getStatus() == GameRoom.Status.IDLE){
			}
		}
		return false;
	}
	
	public boolean exitRoom(Object playerSessionId){
		PlayerSession playerSession = playerSessionManager.get(playerSessionId);
		if (playerSession != null && playerSession.getRoomId() == null) {
			GameRoom room = getGameRoom(playerSession.getRoomId());
			playerSession.close();
		}
		return false;
	}
	
	
	
	
	
}
