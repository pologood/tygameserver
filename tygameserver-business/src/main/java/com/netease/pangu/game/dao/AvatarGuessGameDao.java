package com.netease.pangu.game.dao;

import com.netease.pangu.game.meta.AvatarGuessGame;

public interface AvatarGuessGameDao {
	public AvatarGuessGame getAvatarGuessGameByAvatarId(long gameId, long avatarId);
	public boolean insertAvatarGuessGame(AvatarGuessGame guessGame);
	public boolean saveAvatarGuessGame(AvatarGuessGame guessGame);
}
