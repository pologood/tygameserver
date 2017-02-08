package com.netease.pangu.game.service;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.meta.Avatar;
import io.netty.channel.ChannelId;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AvatarSessionService extends AbstractAvatarSessionService<Avatar> {
    @Resource
    private RoomService roomService;

    public void updateAvatarSessionToNotConnectedByChannelId(ChannelId id){
        updateAvatarSessionByChannelId(id, new AbstractAvatarSessionService.SessionCallable<Void, Avatar>() {
            @Override
            public Void call(AvatarSession<Avatar> playerSession) {
                roomService.exitRoom(playerSession.getAvatarId());
                remove(playerSession.getAvatarId());
                return null;
            }
        });
    }
}
