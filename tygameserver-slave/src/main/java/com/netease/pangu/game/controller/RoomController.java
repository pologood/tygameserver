package com.netease.pangu.game.controller;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.AvatarStatus;
import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.common.meta.IAvatar;
import com.netease.pangu.game.common.meta.RoomStatus;
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.rpc.WsRpcResponse;
import com.netease.pangu.game.rpc.annotation.WsRpcCall;
import com.netease.pangu.game.rpc.annotation.WsRpcController;
import com.netease.pangu.game.service.AbstractAvatarSessionService;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.AvatarSessionService;
import com.netease.pangu.game.service.RoomBroadcastApi;
import com.netease.pangu.game.service.RoomService;
import com.netease.pangu.game.util.BusinessCode;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@HttpController(value = "/room", moduleId = GameConst.SYSTEM)
@WsRpcController(value = "/room", moduleId = GameConst.SYSTEM)
public class RoomController {
    @Resource
    private AvatarSessionService avatarSessionService;
    @Resource
    private AvatarService avatarService;
    @Resource
    private RoomService roomService;

    @HttpRequestMapping(value = "/list")
    @WsRpcCall("/list")
    public GameResult listRoom() {
        Map<Long, Object> ret = new HashMap<Long, Object>();
        for (Map.Entry<Long, GameRoom> entry : roomService.getRooms().entrySet()) {
            ret.put(entry.getKey(), roomService.getRoomInfoMap(entry.getKey()));
        }
        return ReturnUtils.succ(ret);
    }

    @WsRpcCall("/create")
    public GameResult createRoom(int maxSize, GameContext<AvatarSession<Avatar>> ctx) {
        AvatarSession<Avatar> session = ctx.getSession();
        long roomId = roomService.createRoom(ctx.getGameId(), session.getAvatarId(), maxSize);
        GameResult result;
        if (maxSize > 8) {
            result = BusinessCode.failed(BusinessCode.ROOM_MAX_SIZE_EXCEED);
        } else {
            if (roomId > 0) {
                session.setAvatarStatus(AvatarStatus.READY);
                roomService.broadcast(RoomBroadcastApi.ROOM_INFO, roomId, roomService.getRoomInfo(roomId));
                result = ReturnUtils.succ(roomId);
            } else {
                result = BusinessCode.failed(BusinessCode.ROOM_CREATE_FAILED);
            }
        }
        return result;
    }

    @WsRpcCall("/join")
    public GameResult joinRoom(long roomId, GameContext<AvatarSession<Avatar>> ctx) {
        AvatarSession<Avatar> session = ctx.getSession();
        if (session.getRoomId() > 0) {
            return BusinessCode.failed(BusinessCode.ROOM_HAS_JOINED);
        }
        GameRoom room = roomService.getGameRoom(roomId);
        if (room == null) {
            return BusinessCode.failed(BusinessCode.ROOM_NOT_EXIST);
        } else {
            if (room.getStatus() != RoomStatus.IDLE) {
                return BusinessCode.failed(BusinessCode.ROOM_IS_NOT_IN_JOIN_STATE);
            }

            if (room.getSessionIds().size() == room.getMaxSize()) {
                return BusinessCode.failed(BusinessCode.ROOM_IS_FULL);
            }
            int pos = roomService.joinRoom(session.getAvatarId(), roomId);
            GameResult result;
            if (pos >= 0) {
                roomService.broadcast(RoomBroadcastApi.ROOM_JOIN, roomId, roomService.getMember(session));
                result = roomService.getRoomInfo(roomId);
            } else {
                result = BusinessCode.failed(BusinessCode.ROOM_JOINED_FAILED);
            }
            return result;
        }
    }

    @WsRpcCall("/remove")
    public GameResult removeMember(long avatarId, GameContext<AvatarSession<Avatar>> ctx) {
        AvatarSession<Avatar> session = ctx.getSession();
        GameResult result = BusinessCode.failed(BusinessCode.FAILED);
        if (session.getRoomId() > 0) {
            GameRoom room = roomService.getGameRoom(session.getRoomId());
            if (session.getAvatarId() == room.getOwnerId()) {
                if (room.getStatus() == RoomStatus.IDLE) {
                    avatarSessionService.updateAvatarSession(avatarId, new AbstractAvatarSessionService.SessionCallable<Boolean, Avatar>() {
                        @Override
                        public Boolean call(AvatarSession<Avatar> playerSession) {
                            playerSession.setAvatarStatus(AvatarStatus.REMOVE);
                            roomService.chatTo(RoomBroadcastApi.ROOM_REMOVE, room.getId(), Arrays.asList(avatarId), ReturnUtils.succ());
                            playerSession.close();
                            return true;
                        }
                    });

                    result = ReturnUtils.succ(avatarId);
                } else {
                    result = BusinessCode.failed(BusinessCode.ROOM_REMOVE_GAME_RUNNING);
                }
            }
        } else {
            result = BusinessCode.failed(BusinessCode.ROOM_REMOVE_ONLY_BY_OWNER);
        }
        return result;
    }

    @WsRpcCall("/info")
    public GameResult getRoom(long roomId) {
        return roomService.getRoomInfo(roomId);
    }

    @WsRpcCall("/chat")
    public void chat(long roomId, String msg, GameContext<AvatarSession<IAvatar>> ctx) {
        AvatarSession<IAvatar> pSession = ctx.getSession();
        GameRoom room = roomService.getGameRoom(roomId);
        Map<Long, AvatarSession<Avatar>> members = avatarSessionService.getAvatarSessions(Arrays.asList(room.getSessionIds().values().toArray(new Long[0])));
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("msg", msg);
        Map<String, Object> source = new HashMap<String, Object>();
        source.put("uuid", pSession.getUuid());
        IAvatar avatar = pSession.getAvatar();
        source.put("avatarName", avatar.getName());
        GameResult result = ReturnUtils.succ(payload, source);
        for (AvatarSession<Avatar> member : members.values()) {
            if (member.getChannel() != null && member.getChannel().isActive()) {
                WsRpcResponse response = WsRpcResponse.create(ctx.getRpcMethod());
                response.setContent(result);
                member.sendJSONMessage(response);
            }
        }
    }

}
