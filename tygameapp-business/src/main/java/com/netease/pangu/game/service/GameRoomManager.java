package com.netease.pangu.game.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.netease.pangu.game.meta.GameRoom;
import com.netease.pangu.game.meta.PlayerSession;
import com.netease.pangu.game.meta.GameRoom.RoomType;
import com.netease.pangu.game.meta.GameRoom.Status;
import com.netease.pangu.game.service.PlayerSessionManager.SessionCallable;

@Component
public class GameRoomManager {
	@Resource
	private PlayerSessionManager playerSessionManager;
	@Resource
	private UniqueIDGeneratorService uniqueIdGeneratorService;

	private final ConcurrentMap<Long, GameRoom> rooms = new ConcurrentHashMap<Long, GameRoom>();
	private final ConcurrentMap<Long, Set<Long>> gameIdRefRoom = new ConcurrentHashMap<Long, Set<Long>>();

	public boolean remove(long roomId) {
		GameRoom room = rooms.remove(roomId);
		if (room != null) {
			Set<Long> refRooms = gameIdRefRoom.get(room.getGameId());
			if (refRooms != null) {
				gameIdRefRoom.get(room.getGameId()).remove(roomId);
			}
			return true;
		}
		return false;
	}

	private GameRoom getGameRoom(long roomId) {
		return rooms.get(roomId);
	}

	/**
	 * 
	 * @param gameId
	 * @param playerSessionId
	 * @param type
	 * @return roomId
	 */
	public long createRoom(final long gameId, final long playerSessionId, final int maxSize) {
		return playerSessionManager.updatePlayerSession(playerSessionId, new SessionCallable<Long>() {
			@Override
			public Long call(PlayerSession playerSession) {
				if (playerSession.getRoomId() == 0) {
					long roomId = uniqueIdGeneratorService.generate();
					GameRoom room = new GameRoom();
					room.setId(roomId);
					room.setGameId(gameId);
					room.setOwnerId(playerSessionId);
					room.setPlayerSessionIds(new HashSet<Long>());
					room.setStatus(Status.IDLE);
					room.setMaxSize(maxSize);

					rooms.put(roomId, room);
					gameIdRefRoom.putIfAbsent(gameId, new HashSet<Long>());
					gameIdRefRoom.get(gameId).add(roomId);
					return roomId;
				}
				return -1L;
			}
		});
	}

	public boolean canJoin(long roomId) {
		GameRoom room = getGameRoom(roomId);
		return room != null && room.getStatus() == GameRoom.Status.IDLE 
				&& room.getPlayerSessionIds().size() < room.getMaxSize() ? true : false;
	}

	/**
	 * 
	 * @param playerSessionId
	 * @param roomId
	 * @return
	 */
	public boolean joinRoom(final long playerSessionId, final long roomId) {
		return playerSessionManager.updatePlayerSession(playerSessionId, new SessionCallable<Boolean>() {
			@Override
			public Boolean call(PlayerSession playerSession) {
				if (playerSession.getRoomId() == 0) {
					GameRoom room = getGameRoom(roomId);
					if (canJoin(roomId)) {
						playerSession.setRoomId(roomId);
						room.getPlayerSessionIds().add(playerSessionId);
						return true;
					}
				}
				return false;
			}
		});
	}
	
	public boolean joinRandomRoom(final long playerSessionId){
		List<Long> gameRooms = new ArrayList<Long>();
		for(GameRoom room : rooms.values()){
			if(room.getType() == RoomType.PUBLIC && canJoin(room.getId())){
				gameRooms.add(room.getId());
			}
		}
		Random random  = new Random(System.currentTimeMillis());
		long roomId = gameRooms.get(random.nextInt(gameRooms.size()));
		return joinRoom(playerSessionId, roomId);
	}

	/**
	 * 
	 * @param playerSessionId
	 * @return
	 */
	public boolean exitRoom(final long playerSessionId) {
		return playerSessionManager.updatePlayerSession(playerSessionId, new SessionCallable<Boolean>() {
			@Override
			public Boolean call(PlayerSession playerSession) {
				if (playerSession.getRoomId() > 0) {
					GameRoom room = getGameRoom(playerSession.getRoomId());
					room.getPlayerSessionIds().remove(playerSessionId);
					if (room.getPlayerSessionIds().size() > 0) {
						if (room.getOwnerId() == playerSessionId) {
							room.setOwnerId(room.getPlayerSessionIds().toArray(new Long[0])[0]);
						}
					} else {
						room.setOwnerId(0L);
						room.setStatus(Status.CLOSING);
					}
					playerSession.setRoomId(0L);
					return true;
				}
				return false;
			}
		});
	}

}
