package com.netease.pangu.game.service;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.meta.GuessGame;
import com.netease.pangu.game.meta.GuessGame.Guess;
import com.netease.pangu.game.meta.GuessGame.Question;
import com.netease.pangu.game.meta.GuessQuestion;
import com.netease.pangu.game.util.ObjectUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GuessGameService {
    private final ConcurrentMap<Long, GuessGame> gameMap = new ConcurrentHashMap<Long, GuessGame>();

    @Resource
    private RoomService roomService;

    public final List<GuessQuestion> questions = new ArrayList<GuessQuestion>();

    @PostConstruct
    public void init() {
        try {
            List<String> questionsStrList = IOUtils.readLines(this.getClass().getClassLoader().getResourceAsStream("guess_question.csv"), "gbk");
            for (int i = 0; i < questionsStrList.size(); i++) {
                if (i == 0) {
                    continue;
                }
                GuessQuestion question = new GuessQuestion();
                String[] items = questionsStrList.get(i).split(",");
                if (items.length == 3) {
                    question.setAnswer(items[0].trim());
                    question.setHint1(items[1].trim());
                    question.setHint2(items[2].trim());
                    questions.add(question);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public final static long gameId = 1;

    public boolean createGuessGame(long roomId, long avatarId) {
        if (!gameMap.containsKey(roomId)) {
            GuessGame game = new GuessGame();
            game.setGameId(gameId);
            game.setDrawerId(0);
            game.setRoomId(roomId);
            game.setDrawerId(avatarId);
            game.setState(GuessGame.QUESTION_CHOOSING);
            game.setStartTime(System.currentTimeMillis());
            return gameMap.putIfAbsent(roomId, game) == null;
        }
        return false;
    }

    public void addGuessGameAnswer(long roomId, Guess guess) {
        GuessGame game = gameMap.get(roomId);
        synchronized (game) {
            game.getAnswers().add(guess);
        }
    }

    public boolean isDrawer(long roomId, GameContext<AvatarSession<Avatar>> ctx) {
        GuessGame game = getGuessGame(roomId);
        return game != null && ctx.getSession().getAvatarId() == game.getDrawerId();
    }


    public boolean isCorrectAnswer(long roomId, Guess guess) {
        GuessGame game = gameMap.get(roomId);
        return StringUtils.equals(game.getQuestion().getAnswer().trim(), guess.getAnswer().trim());
    }

    public List<Guess> getAnswers(long roomId) {
        return getGuessGame(roomId).getAnswers();
    }

    public int getGuessGameState(long roomId) {
        GuessGame game = gameMap.get(roomId);
        return game.getState();
    }

    public void setGuessGameQuestion(long roomId, Question question) {
        GuessGame game = gameMap.get(roomId);
        synchronized (game) {
            if (game.getState() == GuessGame.QUESTION_CHOOSING) {
                game.setQuestion(question);
                game.setState(GuessGame.QUESTION_OK);
            }
        }
    }

    public void setDrawer(long roomId, long avatarId) {
        GuessGame game = gameMap.get(roomId);
        synchronized (game) {
            game.setDrawerId(avatarId);
            game.setState(GuessGame.QUESTION_CHOOSING);
        }
    }

    public GuessGame getGuessGame(long roomId) {
        return ObjectUtil.deepCopy(gameMap.get(roomId));
    }

    public boolean exit(long roomId) {
        return gameMap.remove(roomId) != null;
    }

    public long generateDrawer(long roomId) {
        GameRoom room = roomService.getGameRoom(roomId);
        Long[] avatarIds = room.getSessionIds().toArray(new Long[0]);
        int random = RandomUtils.nextInt(0, avatarIds.length);
        return avatarIds[random];
    }

    public List<GuessQuestion> getQuestions() {
        return questions;
    }

}
