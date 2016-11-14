package com.netease.pangu.game.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.common.meta.GameRoom.RoomType;
import com.netease.pangu.game.common.meta.GameRoom.Status;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.service.AbstractAvatarSessionService.SessionCallable;
import com.netease.pangu.game.util.NettyHttpUtil;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

@Component
public class RoomService {
	@Resource
	private AvatarSessionService avatarSessionService;
	@Resource
	private UniqueIDGeneratorService uniqueIdGeneratorService;
	@Resource
	private RoomAllocationService roomAllocationService;

	private final ConcurrentMap<Long, GameRoom> rooms = new ConcurrentHashMap<Long, GameRoom>();
	private final ConcurrentMap<Long, Set<Long>> gameIdRefRoom = new ConcurrentHashMap<Long, Set<Long>>();

	public boolean remove(long roomId) {
		GameRoom room = rooms.remove(roomId);
		if (room != null) {
			Set<Long> refRooms = gameIdRefRoom.get(room.getGameId());
			if (refRooms != null) {
				gameIdRefRoom.get(room.getGameId()).remove(roomId);
			}
			roomAllocationService.returnRoom(room.getGameId(), roomId);
			return true;
		}
		return false;
	}

	public GameRoom getGameRoom(long roomId) {
		return rooms.get(roomId);
	}

	public Map<Long, GameRoom> getRooms() {
		return Collections.unmodifiableMap(rooms);
	}

	public Long generateRoomId(long gameId, String server) {
		return roomAllocationService.borrowRoom(gameId, server);

	}

	/**
	 * 
	 * @param gameId
	 * @param avatarId
	 * @param type
	 * @return roomId
	 */
	public long createRoom(final long gameId, final long avatarId, final int maxSize) {
		return avatarSessionService.updateAvatarSession(avatarId, new SessionCallable<Long, Avatar>() {
			@Override
			public Long call(AvatarSession<Avatar> playerSession) {
				if (playerSession.getRoomId() == 0) {
					Long roomId = generateRoomId(gameId, playerSession.getServer());
					if (roomId != null) {
						GameRoom room = new GameRoom();
						room.setId(roomId);
						room.setGameId(gameId);
						room.setOwnerId(avatarId);
						room.setSessionIds(new HashSet<Long>());
						room.setStatus(Status.IDLE);
						room.setMaxSize(maxSize);
						room.getSessionIds().add(avatarId);

						playerSession.setRoomId(roomId);

						rooms.put(roomId, room);
						gameIdRefRoom.putIfAbsent(gameId, new HashSet<Long>());
						gameIdRefRoom.get(gameId).add(roomId);
						return roomId;
					}
				}
				return -1L;
			}
		});
	}

	public boolean canJoin(long roomId) {
		GameRoom room = getGameRoom(roomId);
		return room != null && room.getStatus() == GameRoom.Status.IDLE
				&& room.getSessionIds().size() < room.getMaxSize() ? true : false;
	}

	/**
	 * 
	 * @param avatarId
	 * @param roomId
	 * @return
	 */
	public boolean joinRoom(final long avatarId, final long roomId) {
		return avatarSessionService.updateAvatarSession(avatarId, new SessionCallable<Boolean, Avatar>() {
			@Override
			public Boolean call(AvatarSession<Avatar> playerSession) {
				if (playerSession.getRoomId() == 0) {
					GameRoom room = getGameRoom(roomId);
					if (canJoin(roomId)) {
						playerSession.setRoomId(roomId);
						room.getSessionIds().add(avatarId);
						return true;
					}
				}
				return false;
			}
		});
	}

	public boolean joinRandomRoom(final long avatarId) {
		List<Long> gameRooms = new ArrayList<Long>();
		for (GameRoom room : rooms.values()) {
			if (room.getType() == RoomType.PUBLIC && canJoin(room.getId())) {
				gameRooms.add(room.getId());
			}
		}
		Random random = new Random(System.currentTimeMillis());
		long roomId = gameRooms.get(random.nextInt(gameRooms.size()));
		return joinRoom(avatarId, roomId);
	}

	/**
	 * 
	 * @param playerSessionId
	 * @return
	 */
	public boolean exitRoom(final long avatarId) {
		return avatarSessionService.updateAvatarSession(avatarId, new SessionCallable<Boolean, Avatar>() {
			@Override
			public Boolean call(AvatarSession<Avatar> playerSession) {
				if (playerSession.getRoomId() > 0) {
					GameRoom room = getGameRoom(playerSession.getRoomId());
					room.getSessionIds().remove(avatarId);
					if (room.getSessionIds().size() > 0) {
						if (room.getOwnerId() == avatarId) {
							room.setOwnerId(room.getSessionIds().toArray(new Long[0])[0]);
						}
					} else {
						room.setOwnerId(0L);
						room.setStatus(Status.CLOSING);
						remove(room.getId());
					}
					playerSession.setRoomId(0L);
					return true;
				}
				return false;
			}
		});
	}
	public static final String ROOM_BROADCAST = "/room/broadcast";
	public void broadcast(long roomId, Object msg) {
		GameRoom room = getGameRoom(roomId);
		if (room.getStatus() == Status.IDLE) {
			Set<Long> sessionIds = room.getSessionIds();
			Map<Long, AvatarSession<Avatar>> sessionMap = avatarSessionService.getAvatarSesssions(sessionIds);
			for(AvatarSession<Avatar> session: sessionMap.values()){
				if(session.getChannel() != null && session.getChannel().isActive()){
					NettyHttpUtil.sendWsResponse(ROOM_BROADCAST, session.getChannel(), msg);
				}
			}
		}
	}
	
	public GameResult getRoomInfo(long roomId){
		GameRoom room = getGameRoom(roomId);
		Map<String, Object> payload = new HashMap<String, Object>();
		Map<Long, Avatar>  players = avatarSessionService.getAvatars(room.getSessionIds());
		payload.put("members", players);
		payload.put("id", room.getId());
		payload.put("state", room.getStatus().ordinal());
		payload.put("maxSize", room.getMaxSize());
		payload.put("count", room.getSessionIds().size());
		payload.put("owner", avatarSessionService.getSession(room.getOwnerId()).getName());
		GameResult result = ReturnUtils.succ(payload);
		return result;
	}

}
