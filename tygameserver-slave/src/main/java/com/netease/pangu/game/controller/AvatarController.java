package com.netease.pangu.game.controller;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.AvatarStatus;
import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.distribution.service.SystemAttrService;
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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@HttpController(value = "/avatar", moduleId = GameConst.SYSTEM)
@WsRpcController(value = "/avatar", moduleId = GameConst.SYSTEM)
public class AvatarController {
    @Resource
    private AvatarSessionService avatarSessionService;
    @Resource
    private AvatarService avatarService;
    @Resource
    private RoomService roomService;
    @Resource
    private SystemAttrService systemAttrService;


    @HttpRequestMapping("/list")
    @WsRpcCall("/list")
    public GameResult list() {
        TreeMap<Long, Avatar> map = new TreeMap<Long, Avatar>();
        for (Long avatarId : avatarSessionService.getSessions().keySet()) {
            map.put(avatarId, avatarSessionService.getSessions().get(avatarId).getAvatar());
        }
        GameResult result = ReturnUtils.succ(map);
        return result;
    }

    @WsRpcCall("/exit")
    public GameResult exit(GameContext<AvatarSession<Avatar>> ctx) {
        AvatarSession<Avatar> session = ctx.getSession();
        GameResult result = BusinessCode.failed();
        if (session.getAvatarStatus() != AvatarStatus.GAMING) {
            if (roomService.exitRoom(session.getAvatarId())) {
                roomService.broadcast(RoomBroadcastApi.ROOM_EXIT, session.getRoomId(), roomService.getMember(session));
                result = ReturnUtils.succ(session.getAvatarId());
            }
        }
        return result;
    }

    @WsRpcCall("/ready")
    public GameResult ready(GameContext<AvatarSession<Avatar>> ctx) {
        AvatarSession<Avatar> session = ctx.getSession();
        boolean isOk = avatarSessionService.updateAvatarSession(session.getAvatarId(), new AbstractAvatarSessionService.SessionCallable<Boolean, Avatar>() {

            @Override
            public Boolean call(AvatarSession<Avatar> playerSession) {
                if (session.getAvatarStatus() != AvatarStatus.READY) {
                    session.setAvatarStatus(AvatarStatus.READY);
                    roomService.broadcast(RoomBroadcastApi.ROOM_READY, session.getRoomId(), roomService.getMember(session));
                    return true;
                } else {
                    return false;
                }

            }
        });
        if (isOk) {
            return ReturnUtils.succ(session.getAvatarId());
        } else {
            return BusinessCode.failed();
        }
    }

    @WsRpcCall("/chat")
    public void chat(long sessionId, String msg, GameContext<AvatarSession<Avatar>> context) {
        AvatarSession<Avatar> session = avatarSessionService.getSession(sessionId);
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("msg", msg);
        Map<String, Object> source = new HashMap<String, Object>();
        source.put("uuid", context.getSession().getUuid());
        source.put("playerName", context.getSession().getName());
        GameResult result = ReturnUtils.succ(payload, source);
        WsRpcResponse response = WsRpcResponse.create(context.getRpcMethod());
        response.setContent(result);
        session.sendJSONMessage(response);
    }

}
