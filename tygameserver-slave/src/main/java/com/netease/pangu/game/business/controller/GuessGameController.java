package com.netease.pangu.game.business.controller;

import javax.annotation.Resource;

import org.apache.commons.lang3.RandomUtils;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.meta.GuessGame.Guess;
import com.netease.pangu.game.meta.GuessGame.Question;
import com.netease.pangu.game.rpc.annotation.WsRpcCall;
import com.netease.pangu.game.rpc.annotation.WsRpcController;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.GuessGameService;
import com.netease.pangu.game.service.RoomService;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

@WsRpcController("/guess")
public class GuessGameController {
	@Resource
	private GuessGameService guessGameService;

	@Resource
	private AvatarService avatarService;

	@Resource
	private RoomService roomService;
	
	@WsRpcCall("/create")
	public GameResult createGuessGame(long roomId) {
		long avatarId = generateDrawer(roomId);
		if (guessGameService.createGuessGame(roomId, avatarId)) {
			roomService.broadcast("startGame", roomId, avatarId);
			return ReturnUtils.succ("succ");
		} else {
			return ReturnUtils.failed("failed");
		}
	}
	
	@WsRpcCall("/replay")
	public void generate(long roomId){
		long avatarId = generateDrawer(roomId);
		guessGameService.setDrawer(roomId, avatarId);
		roomService.broadcast("startGame", roomId, avatarId);
	}
	
	public long generateDrawer(long roomId){
		Long[] avatarIds = roomService.getGameRoom(roomId).getSessionIds().toArray(new Long[0]);
		int random = RandomUtils.nextInt(0, avatarIds.length);
		return avatarIds[random];
	}

	@WsRpcCall("/question")
	public GameResult setQuestion(long roomId, String answer, String hint1, String hint2) {
		Question question = new Question();
		question.setAnswer(answer);
		question.setHint1(hint1);
		question.setHint2(hint2);
		guessGameService.setGuessGameQuestion(roomId, question);
		return ReturnUtils.succ(question);
	}

	@WsRpcCall("/answer")
	public GameResult setAnswer(long roomId, String answer, GameContext<AvatarSession<Avatar>> ctx) {
		Guess guess = new Guess();
		guess.setAnswer(answer);
		guess.setAvatarId(ctx.getSession().getAvatarId());
		if (guessGameService.addGuessGameAnswer(roomId, guess)) {
			roomService.broadcast("answer", roomId, guessGameService.getAnswers(roomId));
			return ReturnUtils.succ();
		} else {
			return ReturnUtils.failed();
		}
	}
}
