package com.netease.pangu.guess.service;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.service.AbstractAvatarSessionService;
import com.netease.pangu.game.service.AvatarSessionService;
import com.netease.pangu.game.service.RoomBroadcastApi;
import com.netease.pangu.game.service.RoomService;
import com.netease.pangu.game.util.ReturnUtils;
import io.netty.channel.ChannelId;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by huangc on 2017/3/10.
 */
@Service
public class GuessSessionService {
    @Resource
    private AvatarSessionService avatarSessionService;

    @Resource
    private RoomService roomService;
    @Resource
    private GuessGameService guessGameService;

    public void updateAvatarSessionToNotConnectedByChannelId(ChannelId id) {
        avatarSessionService.updateAvatarSessionByChannelId(id, new AbstractAvatarSessionService.SessionCallable<Void, Avatar>() {
            @Override
            public Void call(AvatarSession<Avatar> playerSession) {
                GameRoom room = roomService.getGameRoom(playerSession.getRoomId());
                if (roomService.exitRoom(playerSession.getAvatarId())) {
                    ReturnUtils.GameResult result = guessGameService.exit(room.getId(), playerSession);
                    if (result.getCode() == ReturnUtils.FAILED) {
                        roomService.broadcast(RoomBroadcastApi.ROOM_REMOVE, room.getId(), ReturnUtils.succ(playerSession.getAvatarId()));
                    } else {
                        roomService.broadcast(RoomBroadcastApi.GAME_EXIT, room.getId(), ReturnUtils.succ(playerSession.getAvatarId()));
                    }

                    if (room != null && room.getSessionIds().size() == 1) {
                        guessGameService.stopGame(room.getId());
                    }
                }
                avatarSessionService.remove(playerSession.getAvatarId());
                return null;
            }
        });
    }
}
