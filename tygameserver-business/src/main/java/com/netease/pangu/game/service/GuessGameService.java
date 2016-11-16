package com.netease.pangu.game.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import com.netease.pangu.game.meta.GuessGame;
import com.netease.pangu.game.meta.GuessGame.Guess;
import com.netease.pangu.game.meta.GuessGame.Question;
import com.netease.pangu.game.util.ObjectUtil;

@Service
public class GuessGameService {
	private final ConcurrentMap<Long, GuessGame> gameMap = new ConcurrentHashMap<Long, GuessGame>();
	public final static long gameId = 1;
	public boolean createGuessGame(long roomId, long avatarId){
		if(!gameMap.containsKey(roomId)){
			GuessGame game = new GuessGame();
			game.setGameId(gameId);
			game.setStartTime(GuessGame.UNREADY);
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
			game.setState(GuessGame.START);
			game.setDrawerId(avatarId);
		}
	}
	
	public GuessGame getGuessGame(long roomId){
		return ObjectUtil.deepCopy(gameMap.get(roomId));
	}
	
	public boolean exit(long roomId){
		return gameMap.remove(roomId) != null;
	}
	
}
