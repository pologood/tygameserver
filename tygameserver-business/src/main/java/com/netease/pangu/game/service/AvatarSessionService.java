package com.netease.pangu.game.service;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.util.ReturnUtils;
import io.netty.channel.ChannelId;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AvatarSessionService extends AbstractAvatarSessionService<Avatar> {
    @Resource
    private RoomService roomService;
    @Resource
    private GuessGameService guessGameService;
    public void updateAvatarSessionToNotConnectedByChannelId(ChannelId id){
        updateAvatarSessionByChannelId(id, new AbstractAvatarSessionService.SessionCallable<Void, Avatar>() {
            @Override
            public Void call(AvatarSession<Avatar> playerSession) {
                GameRoom room = roomService.getGameRoom(playerSession.getRoomId());
                if(roomService.exitRoom(playerSession.getAvatarId())) {
                    remove(playerSession.getAvatarId());
                    ReturnUtils.GameResult result = guessGameService.exit(room.getId(), playerSession);

                    if(result.getCode() == ReturnUtils.FAILED) {
                        roomService.broadcast(RoomBroadcastApi.ROOM_REMOVE, room.getId(), ReturnUtils.succ(playerSession.getAvatarId()));
                    }else{
                        roomService.broadcast(RoomBroadcastApi.GAME_EXIT, room.getId(), ReturnUtils.succ(playerSession.getAvatarId()));
                    }

                    if (room != null && room.getSessionIds().size() == 0) {
                        guessGameService.stopGame(room.getId());
                    }

                }
                return null;
            }
        });
    }
}
