package com.netease.pangu.game.service;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.meta.GuessGame;
import com.netease.pangu.game.meta.GuessGame.Guess;
import com.netease.pangu.game.meta.GuessGameState;
import com.netease.pangu.game.meta.GuessQuestion;
import com.netease.pangu.game.util.ObjectUtil;
import com.netease.pangu.game.util.ReturnUtils;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
public class GuessGameService {
    private final ConcurrentMap<Long, GuessGame> gameMap = new ConcurrentHashMap<Long, GuessGame>();

    @Resource
    private RoomService roomService;

    private final List<GuessQuestion> questions = new ArrayList<GuessQuestion>();

    public final static long gameId = 1;
    private final static int TOTOAL_ROUND = 8;
    private final static int ROUND_INTERVAL_TIME = 5000;
    private final static int ROUNG_GAME_TIME = 60000;
    private final static Map<GuessGame.RULE, Integer> RULE_SCORE;

    static {
        RULE_SCORE = new HashMap<GuessGame.RULE, Integer>();
        RULE_SCORE.put(GuessGame.RULE.FIRST_GUESSED, 3);
        RULE_SCORE.put(GuessGame.RULE.GUESSED, 1);
        RULE_SCORE.put(GuessGame.RULE.BE_GUESSED, 1);
        RULE_SCORE.put(GuessGame.RULE.LIKE, 1);
        RULE_SCORE.put(GuessGame.RULE.EXIT, -10);
    }

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


    public boolean startGame(long roomId) {
        long avatarId = generateDrawer(roomId);
        if (!gameMap.containsKey(roomId)) {
            GuessGame game = new GuessGame();
            game.setGameId(gameId);
            game.setRoomId(roomId);
            game.setRound(0);
            game.setState(GuessGameState.START);
            game.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    GuessGame game = gameMap.get(roomId);
                    synchronized (game) {
                        long current = System.currentTimeMillis();
                        if (game.getState() == GuessGameState.START) {
                            game.setDrawerId(generateDrawer(roomId));
                            long startTime = System.currentTimeMillis();
                            game.setStartTime(startTime);
                            game.setNextStartTime(0);
                            game.setEndTime(startTime + ROUNG_GAME_TIME);
                            game.setRound(1);
                            game.setState(GuessGameState.ROUND_GAMING);
                        } else if (game.getState() != GuessGameState.ROUND_INTERNAL) {
                            if (current >= game.getEndTime()) {
                                if(game.getRound() == TOTOAL_ROUND) {
                                    game.setState(GuessGameState.GAME_STATS);
                                    Map<String, Object> ret = new HashMap<String, Object>();
                                    ret.put("lrt", game.getEndTime());
                                    ret.put("answer", game.getQuestion().getAnswer());
                                    roomService.broadcast("/guess/gameover", roomId, ReturnUtils.succ(ret));
                                }else{
                                    game.setState(GuessGameState.ROUND_INTERNAL);
                                    long nextStartTime = current + ROUND_INTERVAL_TIME;
                                    game.setNextStartTime(nextStartTime);
                                    Map<String, Object> ret = new HashMap<String, Object>();
                                    ret.put("lrt", game.getEndTime());
                                    ret.put("nrt", nextStartTime);
                                    ret.put("answer", game.getQuestion().getAnswer());
                                    roomService.broadcast("/guess/roundover", roomId, ReturnUtils.succ(ret));
                                }
                                //save game data
                            }
                        } else if (game.getState() == GuessGameState.ROUND_INTERNAL && game.getRound() < TOTOAL_ROUND) {
                            if (current == game.getNextStartTime()) {
                                game.setState(GuessGameState.ROUND_GAMING);
                                long drawerId = generateDrawer(roomId);
                                game.setDrawerId(drawerId);
                                game.setStartTime(game.getNextStartTime());
                                game.setEndTime(game.getNextStartTime() + ROUNG_GAME_TIME);
                                game.setRound(game.getRound() + 1);
                            }
                        } else if (game.getState() == GuessGameState.GAME_STATS) {

                        }
                    }
                }
            }, 100, TimeUnit.MILLISECONDS);

            if (gameMap.putIfAbsent(roomId, game) == null) {
                game.getTimer().start();
            }
        }
        return false;
    }

    public void answer(long roomId, long avatarId, Guess guess) {

    }

    public void addGuessGameAnswer(long roomId, Guess guess) {
        GuessGame game = gameMap.get(roomId);
        synchronized (game) {
            game.getAnswers().add(guess);
        }
    }

    public boolean addScore(GuessGame.RULE rule, long roomId, long avatarId) {
        GuessGame game = gameMap.get(roomId);
        if (game != null) {
            synchronized (game) {
                Map<Long, List<GuessGame.RULE>> operations = game.getOperations();
                Map<Long, Integer> scores = game.getScores();
                long drawerId = game.getDrawerId();
                if (!operations.containsKey(drawerId)) {
                    operations.put(drawerId, new ArrayList<GuessGame.RULE>());
                }
                if (!operations.containsKey(avatarId)) {
                    operations.put(avatarId, new ArrayList<GuessGame.RULE>());
                }
                if (rule == GuessGame.RULE.GUESSED || rule == GuessGame.RULE.FIRST_GUESSED || rule == GuessGame.RULE.LIKE) {
                    GuessGame.RULE drawRule = null;
                    if (rule == GuessGame.RULE.GUESSED || rule == GuessGame.RULE.FIRST_GUESSED) {
                        drawRule = GuessGame.RULE.BE_GUESSED;
                    } else if (rule == GuessGame.RULE.LIKE) {
                        drawRule = GuessGame.RULE.LIKE;
                    }
                    List<GuessGame.RULE> drawerRuleList = operations.get(drawerId);
                    drawerRuleList.add(drawRule);
                    int drawerScore = MapUtils.getIntValue(scores, rule, 0) + RULE_SCORE.get(drawRule);
                    scores.put(avatarId, drawerScore > 0 ? drawerScore : 0);
                }

                List<GuessGame.RULE> ruleList = operations.get(avatarId);
                ruleList.add(rule);
                int score = MapUtils.getIntValue(scores, rule, 0) + RULE_SCORE.get(rule);
                scores.put(avatarId, score > 0 ? score : 0);
            }
        }
        return false;
    }

    public boolean containsRule(GuessGame.RULE rule, long roomId, long avatarId) {
        GuessGame game = gameMap.get(roomId);
        if (game != null) {
            Map<Long, List<GuessGame.RULE>> operations = game.getOperations();
            List<GuessGame.RULE> ruleList = operations.get(avatarId);
            if (CollectionUtils.isNotEmpty(ruleList)) {
                return ruleList.contains(rule);
            } else {
                return true;
            }
        } else {
            return false;
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

    public GuessGameState getGuessGameState(long roomId) {
        GuessGame game = gameMap.get(roomId);
        return game.getState();
    }

    public boolean isGameOver(long roomId) {
        GuessGame game = gameMap.get(roomId);
        if (game.getEndTime() <= System.currentTimeMillis()) {
            return true;
        } else {
            return false;
        }

    }

    public GuessGame getGuessGame(long roomId) {
        return ObjectUtil.deepCopy(gameMap.get(roomId));
    }


    public boolean exit(long roomId) {
        return gameMap.remove(roomId) != null;
    }

    private long generateDrawer(long roomId) {
        GameRoom room = roomService.getGameRoom(roomId);
        Long[] avatarIds = room.getSessionIds().toArray(new Long[0]);
        int random = RandomUtils.nextInt(0, avatarIds.length);
        return avatarIds[random];
    }

    public List<GuessQuestion> getQuestions() {
        return questions;
    }

}
