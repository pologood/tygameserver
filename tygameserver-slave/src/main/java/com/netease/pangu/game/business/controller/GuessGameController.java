package com.netease.pangu.game.business.controller;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.AvatarStatus;
import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.common.meta.RoomStatus;
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.meta.GuessGame;
import com.netease.pangu.game.meta.GuessGameState;
import com.netease.pangu.game.rpc.annotation.WsRpcCall;
import com.netease.pangu.game.rpc.annotation.WsRpcController;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.AvatarSessionService;
import com.netease.pangu.game.service.GuessGameService;
import com.netease.pangu.game.service.RoomService;
import com.netease.pangu.game.util.BusinessCode;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

import javax.annotation.Resource;
import java.util.Map;

@HttpController(value = "/guess", gameId = GameConst.GUESSS)
@WsRpcController(value = "/guess", gameId = GameConst.GUESSS)
public class GuessGameController {
    public static final String START_GAME = "startGame";
    public static final String DRAW_GAME = "drawGame";
    public static final String QUESTION_GAME = "drawQuestion";

    @Resource
    private GuessGameService guessGameService;
    @Resource
    private AvatarService avatarService;
    @Resource
    private AvatarSessionService avatarSessionService;
    @Resource
    private RoomService roomService;

    @HttpRequestMapping("/list")
    public GameResult listGuessGame() {
        return ReturnUtils.succ(guessGameService.getGuessGames());
    }

    @WsRpcCall("/start")
    public GameResult startGuessGame(long roomId, GameContext<AvatarSession<Avatar>> ctx) {
        if (roomService.isReady(roomId) && roomService.isRoomOwner(roomId, ctx.getSession().getAvatarId())) {
            roomService.setRoomState(roomId, RoomStatus.GAMEING, AvatarStatus.GAMING, AvatarStatus.GAMING);
            if (guessGameService.startGame(roomId, ctx.getSession())) {
                return ReturnUtils.succ();
            } else {
                return BusinessCode.failed(BusinessCode.STAT_GAME_FAILED);
            }
        } else {
            return BusinessCode.failed(BusinessCode.ROOM_STATE_NOT_SUPPORT_START);
        }
    }

    @WsRpcCall("/draw")
    public GameResult draw(long roomId, Map<String, Object> content, GameContext<AvatarSession<Avatar>> ctx) {
        if (guessGameService.isDrawer(roomId, ctx.getSession().getAvatarId())) {
            GuessGame game = guessGameService.getGuessGame(roomId);
            if (game != null && game.getState() == GuessGameState.ROUND_GAMING && ctx.getSession().getAvatarId() == game.getDrawerId()) {
                roomService.broadcast(DRAW_GAME, roomId, ReturnUtils.succ(content));
                return ReturnUtils.succ();
            } else {
                return BusinessCode.failed();
            }
        } else {
            return BusinessCode.failed();
        }
    }

    @WsRpcCall("/like")
    public GameResult like(long roomId, GameContext<AvatarSession<Avatar>> ctx) {
        long avatarId = ctx.getSession().getAvatarId();
        return guessGameService.like(roomId, ctx.getSession());
    }

    @WsRpcCall("/exit")
    public void exit(long roomId, GameContext<AvatarSession<Avatar>> ctx) {
        guessGameService.exit(roomId, ctx.getSession());
    }

    @WsRpcCall("/unlike")
    public GameResult unlike(long roomId, GameContext<AvatarSession<Avatar>> ctx) {
        long avatarId = ctx.getSession().getAvatarId();
        return guessGameService.unlike(roomId, ctx.getSession());
    }

    @WsRpcCall("/answer")
    public GameResult setAnswer(long roomId, String answer, GameContext<AvatarSession<Avatar>> ctx) {
        GuessGame.Guess guess = new GuessGame.Guess();
        long avatarId = ctx.getSession().getAvatarId();
        guess.setAvatarId(avatarId);
        guess.setAnswer(answer);
        guess.setAvatarName(ctx.getSession().getName());
        guess.setTime(System.currentTimeMillis());
        if (guessGameService.getGuessGameState(roomId) == GuessGameState.ROUND_GAMING) {
            guessGameService.answer(roomId, ctx.getSession(), guess);
            return ReturnUtils.succ();
        } else {
            return BusinessCode.failed(BusinessCode.GUESS_NOTIN_ANSWER_TIME);
        }
    }
}
