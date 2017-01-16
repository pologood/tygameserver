package com.netease.pangu.game.service;

import com.netease.pangu.game.common.meta.IAvatar;

import javax.annotation.Resource;

public abstract class AbstractAvatarService<A extends IAvatar> {
    @Resource
    private UniqueIDGeneratorService uniqueIdGeneratorService;

    public AbstractAvatarService() {
    }

    public A createAvatar(A avatar) {
        long avatarId = uniqueIdGeneratorService.generateAvatarId();
        avatar.setAvatarId(avatarId);
        return put(avatarId, avatar);
    }

    protected abstract A put(long avatarId, A avatar);


    protected abstract A get(long avatarId);
}
