package com.netease.pangu.game.business.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.RandomUtils;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.meta.GuessGame;
import com.netease.pangu.game.meta.GuessGame.Guess;
import com.netease.pangu.game.meta.GuessGame.Question;
import com.netease.pangu.game.rpc.annotation.WsRpcCall;
import com.netease.pangu.game.rpc.annotation.WsRpcController;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.AvatarSessionService;
import com.netease.pangu.game.service.GuessGameService;
import com.netease.pangu.game.service.RoomService;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

@WsRpcController("/guess")
public class GuessGameController {
	@Resource
	private GuessGameService guessGameService;
	public static final String START_GAME = "startGame";
	public static final String DRAW_GAME = "drawGame";
	public static final String QUESTION_GAME = "drawQuestion";
	@Resource
	private AvatarService avatarService;
	@Resource
	private AvatarSessionService avatarSessionService;
	@Resource
	private RoomService roomService;
	
	@WsRpcCall("/create")
	public GameResult createGuessGame(long roomId, GameContext<AvatarSession<Avatar>> ctx) {
		if(isReady(roomId) && roomService.isRoomOwner(roomId, ctx.getSession().getAvatarId())){
			long avatarId = generateDrawer(roomId);
			if (guessGameService.createGuessGame(roomId, avatarId)) {
				roomService.broadcast(START_GAME, roomId, ReturnUtils.succ(avatarId));
				return ReturnUtils.succ("succ");
			} else {
				return ReturnUtils.failed("failed");
			}
		}else{
			return ReturnUtils.failed("room is not ready");
		}
	}
	
	private boolean isReady(long roomId){
		GameRoom room = roomService.getGameRoom(roomId);
		Set<Long> avatarIds = room.getSessionIds();
		Map<Long, AvatarSession<Avatar>> sessionsMap = avatarSessionService.getAvatarSesssions(avatarIds);
		if(sessionsMap.values().size() < 2){
			return false;
		}
		for(AvatarSession<Avatar> session: sessionsMap.values()){
			if(session.getState() != AvatarSession.READY){
				return false;
			}
		}
		return true;
	}
	
	private boolean isDrawer(long roomId, GameContext<AvatarSession<Avatar>> ctx){
		GuessGame game = guessGameService.getGuessGame(roomId);
		return game != null && ctx.getSession().getAvatarId() == game.getDrawerId();
	}
	
	@WsRpcCall("/replay")
	public void generate(long roomId, GameContext<AvatarSession<Avatar>> ctx){
		if(isReady(roomId) && isDrawer(roomId, ctx)){
			long avatarId = generateDrawer(roomId);
			guessGameService.setDrawer(roomId, avatarId);
			roomService.broadcast(START_GAME, roomId,  ReturnUtils.succ(avatarId));
		}else{
			roomService.broadcast(START_GAME, roomId,  ReturnUtils.failed("room is not ready"));
		}
	}
	
	@WsRpcCall("/draw")
	public GameResult draw(long roomId, Map<String, Object> content, GameContext<AvatarSession<Avatar>> ctx){
		if(isReady(roomId) && isDrawer(roomId, ctx)){
			GuessGame game = guessGameService.getGuessGame(roomId);
			if(game != null && ctx.getSession().getAvatarId() == game.getDrawerId()){
				roomService.broadcast(DRAW_GAME, roomId, ReturnUtils.succ(content));
				return ReturnUtils.succ();
			}else{
				return ReturnUtils.failed("you are not drawer");
			}
		}else{
			return ReturnUtils.failed("room is not ready");
		}	
	}

	public long generateDrawer(long roomId){
		GameRoom room =  roomService.getGameRoom(roomId);
		Long[] avatarIds = room.getSessionIds().toArray(new Long[0]);
		int random = RandomUtils.nextInt(0, avatarIds.length);
		return avatarIds[random];
		
	}

	@WsRpcCall("/question")
	public GameResult setQuestion(long roomId, String answer, String hint1, String hint2, GameContext<AvatarSession<Avatar>> ctx) {
		Question question = new Question();
		question.setAnswer(answer);
		question.setHint1(hint1);
		question.setHint2(hint2);
		if(isReady(roomId) && isDrawer(roomId, ctx)){
			guessGameService.setGuessGameQuestion(roomId, question);
			Map<String, Object> questionMap = new HashMap<String, Object>();
			questionMap.put("hint1", hint1);
			questionMap.put("hint2", hint2);
			roomService.broadcast(QUESTION_GAME, roomId, ReturnUtils.succ(questionMap));
			return ReturnUtils.succ(question);
		}else{
			return ReturnUtils.failed();
		}
	
	}

	@WsRpcCall("/answer")
	public GameResult setAnswer(long roomId, String answer, GameContext<AvatarSession<Avatar>> ctx) {
		Guess guess = new Guess();
		guess.setAnswer(answer);
		guess.setAvatarId(ctx.getSession().getAvatarId());
		if (!isDrawer(roomId, ctx) && guessGameService.addGuessGameAnswer(roomId, guess)) {
			roomService.broadcast("answer", roomId, guessGameService.getAnswers(roomId));
			return ReturnUtils.succ();
		} else {
			return ReturnUtils.failed();
		}
	}
}
