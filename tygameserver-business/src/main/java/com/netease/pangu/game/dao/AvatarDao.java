package com.netease.pangu.game.dao;

public interface AvatarDao<IAvatar> {
    public IAvatar getAvatarByAvatarId(long id);

    public IAvatar getAvatarByUUID(long gameId, String uuid);

    public boolean removeAvatar(IAvatar avatar);
}
