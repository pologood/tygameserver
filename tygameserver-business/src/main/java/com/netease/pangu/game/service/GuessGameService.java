package com.netease.pangu.game.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.meta.GuessGame;
import com.netease.pangu.game.meta.GuessGame.Guess;
import com.netease.pangu.game.meta.GuessGame.Question;
import com.netease.pangu.game.util.ObjectUtil;

@Service
public class GuessGameService {
	private final ConcurrentMap<Long, GuessGame> gameMap = new ConcurrentHashMap<Long, GuessGame>();
	
	@Resource private RoomService roomService;
	
	public final static long gameId = 1;
	public boolean createGuessGame(long roomId, long avatarId){
		if(!gameMap.containsKey(roomId)){
			GuessGame game = new GuessGame();
			game.setGameId(gameId);
			game.setDrawerId(0);
			game.setRoomId(roomId);
			game.setDrawerId(avatarId);
			game.setStartTime(System.currentTimeMillis());
			return gameMap.putIfAbsent(roomId, game) == null;
		}
		return false;
	}
	
	public void addGuessGameAnswer(long roomId, Guess guess){
		GuessGame game = gameMap.get(roomId);
		synchronized (game) {
			game.getAnswers().add(guess);
		}
	}
	
	public boolean isDrawer(long roomId, GameContext<AvatarSession<Avatar>> ctx){
		GuessGame game = getGuessGame(roomId);
		return game != null && ctx.getSession().getAvatarId() == game.getDrawerId();
	}
	
	
	public boolean isCorrectAnswer(long roomId, Guess guess){
		GuessGame game = gameMap.get(roomId);
		return StringUtils.equals(game.getQuestion().getAnswer().trim(), guess.getAnswer().trim());
	}
	
	public List<Guess> getAnswers(long roomId){
		return getGuessGame(roomId).getAnswers();
	}
	
	public void setGuessGameQuestion(long roomId, Question question){
		GuessGame game = gameMap.get(roomId);
		synchronized (game) {
			game.setQuestion(question);
		}
	}
	
	public void setDrawer(long roomId, long avatarId){
		GuessGame game = gameMap.get(roomId);
		synchronized (game) {
			game.setDrawerId(avatarId);
		}
	}
	
	public GuessGame getGuessGame(long roomId){
		return ObjectUtil.deepCopy(gameMap.get(roomId));
	}
	
	public boolean exit(long roomId){
		return gameMap.remove(roomId) != null;
	}
	
	public long generateDrawer(long roomId){
		GameRoom room =  roomService.getGameRoom(roomId);
		Long[] avatarIds = room.getSessionIds().toArray(new Long[0]);
		int random = RandomUtils.nextInt(0, avatarIds.length);
		return avatarIds[random];
		
	}
	
}
