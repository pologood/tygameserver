package com.netease.pangu.game.business.controller;

import com.netease.pangu.game.common.meta.*;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.rpc.WsRpcResponse;
import com.netease.pangu.game.rpc.annotation.WsRpcCall;
import com.netease.pangu.game.rpc.annotation.WsRpcController;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.AvatarSessionService;
import com.netease.pangu.game.service.GuessGameService;
import com.netease.pangu.game.service.RoomService;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@WsRpcController(value = "/room", gameId = GameConst.GUESSS)
public class RoomController {
    @Resource
    private AvatarSessionService avatarSessionService;
    @Resource
    private AvatarService avatarService;
    @Resource
    private RoomService roomService;
    @Resource
    private GuessGameService guessGameService;

    @WsRpcCall("/list")
    public GameResult listRoom(GameContext<Avatar> ctx) {
        GameResult result = ReturnUtils.succ(roomService.getRooms());
        return result;
    }

    @WsRpcCall("/create")
    public GameResult createRoom(long gameId, int maxSize, GameContext<AvatarSession<Avatar>> ctx) {
        AvatarSession<Avatar> session = ctx.getSession();
        long roomId = roomService.createRoom(gameId, session.getAvatarId(), maxSize);
        GameResult result;
        if (roomId > 0) {
            session.setAvatarStatus(AvatarStatus.READY);
            roomService.broadcast(RoomService.ROOM_INFO, roomId, roomService.getRoomInfo(roomId));
            result = ReturnUtils.succ(roomId);
        } else {
            result = ReturnUtils.failed("create room failed");
        }
        return result;
    }

    @WsRpcCall("/join")
    public GameResult joinRoom(long roomId, GameContext<AvatarSession<Avatar>> ctx) {
        AvatarSession<Avatar> session = ctx.getSession();
        if (session.getRoomId() > 0) {
            if (session.getRoomId() == roomId) {
                return ReturnUtils.succ(roomId);
            } else {
                return ReturnUtils.failed(String.format("you have in room %d", session.getRoomId()));
            }
        }
        boolean isOk = roomService.joinRoom(session.getAvatarId(), roomId);
        GameResult result;
        if (isOk) {
            roomService.broadcast(RoomService.ROOM_INFO, roomId, roomService.getRoomInfo(roomId));
            result = ReturnUtils.succ(roomId);
        } else {
            result = ReturnUtils.failed(String.format("failed to join %d", roomId));
        }
        return result;
    }

    @WsRpcCall("/remove")
    public GameResult removeMember(long avatarId, GameContext<AvatarSession<Avatar>> ctx) {
        AvatarSession<Avatar> session = ctx.getSession();
        GameResult result = ReturnUtils.failed();
        if (session.getRoomId() > 0) {
            GameRoom room = roomService.getGameRoom(session.getRoomId());
            if (session.getAvatarId() == room.getOwnerId()) {
                if (room.getStatus() == RoomStatus.IDLE) {
                    boolean isOk = roomService.exitRoom(avatarId);
                    if (isOk) {
                        roomService.broadcast(RoomService.ROOM_REMOVE_MEMBER, room.getId(), ReturnUtils.succ(avatarId));
                        result = ReturnUtils.succ(avatarId);
                    } else {
                        result = ReturnUtils.failed(String.format("failed to remove member %d", avatarId));
                    }
                } else {
                    result = ReturnUtils.failed(String.format("room is not idle %d", avatarId));
                }
            }
        } else {
            result = ReturnUtils.failed(String.format(" you(%d) are not a room owner", avatarId));
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
        Map<Long, AvatarSession<Avatar>> members = avatarSessionService.getAvatarSessions(room.getSessionIds());
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("msg", msg);
        Map<String, Object> source = new HashMap<String, Object>();
        source.put("uuid", pSession.getUuid());
        IAvatar avatar = pSession.getAvatar();
        source.put("avatarName", avatar.getName());
        GameResult result = ReturnUtils.succ(payload, source);
        for (AvatarSession<Avatar> member : members.values()) {
            if (member.getChannel() != null && member.getChannel().isActive()) {
                WsRpcResponse response = WsRpcResponse.create(ctx.getRpcMethodName());
                response.setContent(result);
                member.sendJSONMessage(response);
            }
        }
    }

}
