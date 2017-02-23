package com.netease.pangu.game.service;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.AvatarStatus;
import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.common.meta.RoomStatus;
import com.netease.pangu.game.dao.impl.GuessGameInfoDaoImpl;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.meta.GuessGame;
import com.netease.pangu.game.meta.GuessGame.Guess;
import com.netease.pangu.game.meta.GuessGameInfo;
import com.netease.pangu.game.meta.GuessGameState;
import com.netease.pangu.game.meta.GuessQuestion;
import com.netease.pangu.game.util.ObjectUtil;
import com.netease.pangu.game.util.ReturnUtils;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GuessGameService {
    private final ConcurrentMap<Long, GuessGame> gameMap = new ConcurrentHashMap<Long, GuessGame>();

    @Resource
    private RoomService roomService;

    @Resource
    private GuessGameInfoDaoImpl guessGameInfoDao;
    @Resource
    private AvatarService avatarService;
    @Resource
    private AvatarSessionService avatarSessionService;

    private final List<GuessQuestion> questions = new ArrayList<GuessQuestion>();

    private final static int TOTOAL_ROUND = 3;
    private final static int ROUND_INTERVAL_TIME = 5000;
    private final static int ROUNG_GAME_TIME = 60000;
    private final static int PERIOD_TIME = 20;
    private final static Map<GuessGame.RULE, Integer> RULE_SCORE;
    private final Timer checkTimer = new Timer();
    private final TimerTask checkGameStateTask = new TimerTask() {
        @Override
        public void run() {
            try {
                for (GuessGame game : gameMap.values()) {
                    GameRoom room = roomService.getGameRoom(game.getRoomId());
                    if (room == null || game.getState() != GuessGameState.START && room.getSessionIds().size() == 0) {
                        game.getTimer().cancel();
                        gameMap.remove(game.getRoomId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    public void stop() {
        checkTimer.cancel();
    }

    static {
        RULE_SCORE = new HashMap<GuessGame.RULE, Integer>();
        RULE_SCORE.put(GuessGame.RULE.FIRST_GUESSED, 3);
        RULE_SCORE.put(GuessGame.RULE.GUESSED, 1);
        RULE_SCORE.put(GuessGame.RULE.BE_GUESSED, 1);
        RULE_SCORE.put(GuessGame.RULE.BE_LIKED, 1);
        RULE_SCORE.put(GuessGame.RULE.EXIT, -10);
    }

    public Map<Long, Object> getGuessGames() {
        Map<Long, Object> map = new HashMap<Long, Object>();
        for (Long roomId : gameMap.keySet()) {
            Map<String, Object> currentGame = new HashMap<String, Object>();
            GuessGame game = gameMap.get(roomId);
            currentGame.put("drawerId", game.getDrawerId());
            currentGame.put("round", game.getRound());
            currentGame.put("endTime", game.getEndTime());
            currentGame.put("nextStartTime", game.getNextStartTime());
            currentGame.put("scores", game.getScores());
            currentGame.put("answers", game.getAnswers());
            currentGame.put("state", game.getState());
            currentGame.put("isFirstGuessed", game.isFirstGuessed());
            currentGame.put("operations", game.getOperations());
            map.put(roomId, currentGame);
        }
        return Collections.unmodifiableMap(map);
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
            checkTimer.scheduleAtFixedRate(checkGameStateTask, 0, PERIOD_TIME);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static boolean isNearEqual(long t1, long t2) {
        return t1 >= t2 - PERIOD_TIME && t1 < t2 + PERIOD_TIME;
    }

    public class GameTimerTask extends TimerTask {

        private long roomId;
        private GuessGameInfo guessGameInfo;

        public GameTimerTask(long roomId) {
            this.roomId = roomId;
        }

        public GuessGameInfo getGuessGameInfo() {
            return guessGameInfo;
        }

        public void setGuessGameInfo(GuessGameInfo guessGameInfo) {
            this.guessGameInfo = guessGameInfo;
        }

        public long getRoomId() {
            return roomId;
        }

        public void setRoomId(long roomId) {
            this.roomId = roomId;
        }

        @Override
        public void run() {
            try {
                GuessGame game = gameMap.get(roomId);
                GameRoom room = roomService.getGameRoom(roomId);
                if (game != null && room != null) {
                    synchronized (game) {
                        long current = System.currentTimeMillis();
                        if (game.getState() == GuessGameState.START) {
                            game.setDrawerId(generateDrawer(roomId));
                            game.setStartTime(current);
                            game.setNextStartTime(0);
                            game.setEndTime(current + ROUNG_GAME_TIME);
                            game.setRound(1);
                            game.setFirstGuessed(false);
                            game.setState(GuessGameState.ROUND_GAMING);
                            game.setQuestion(generateQuestion());
                            roomService.chatTo(RoomBroadcastApi.GAME_QUESTION, roomId, Arrays.asList(game.getDrawerId()), ReturnUtils.succ(game.getQuestion()));
                            roomService.broadcast(RoomBroadcastApi.GAME_START, roomId, ReturnUtils.succ(getCurrentGameInfo(roomId)));
                        } else if (game.getState() == GuessGameState.ROUND_GAMING) {
                            if (current <= game.getEndTime()) {
                                if (isNearEqual(current, (current / 1000) * 1000)) {
                                    roomService.broadcast(RoomBroadcastApi.GAME_COUNTDOWN, roomId, ReturnUtils.succ(game.getEndTime() / 1000 - current / 1000));
                                }

                                if (isNearEqual(current, game.getStartTime() + 5000)) {
                                    roomService.broadcast(RoomBroadcastApi.GAME_HINT1, roomId, ReturnUtils.succ(game.getQuestion().getHint1()));
                                }

                                if (isNearEqual(current, game.getStartTime() + 15000)) {
                                    roomService.broadcast(RoomBroadcastApi.GAME_HINT2, roomId, ReturnUtils.succ(game.getQuestion().getHint2()));
                                }
                            } else {
                                game.setState(GuessGameState.ROUND_INTERNAL);
                                long nextStartTime = current + ROUND_INTERVAL_TIME;
                                game.setNextStartTime(nextStartTime);
                                Map<String, Object> ret = new HashMap<String, Object>(getCurrentGameInfo(roomId));
                                ret.put("answer", game.getQuestion().getAnswer());
                                roomService.broadcast(RoomBroadcastApi.GAME_ROUND_OVER, roomId, ReturnUtils.succ(ret));

                                getGuessGameInfo().getInfos().put(game.getRound(), new GuessGame.GameRound(game, room.getOwnerId()));
                                guessGameInfoDao.save(getGuessGameInfo());
                            }
                        } else if (game.getState() == GuessGameState.ROUND_INTERNAL && game.getRound() <= room.getSessionIds().size()) {
                            if (isNearEqual(current, (current / 1000) * 1000)) {
                                roomService.broadcast(RoomBroadcastApi.GAME_INTERVAL_COUNTDOWN, roomId, ReturnUtils.succ(game.getNextStartTime() / 1000 - current / 1000));
                            }

                            if (current > game.getNextStartTime()) {
                                if (game.getRound() == room.getSessionIds().size()) {
                                    game.setState(GuessGameState.GAME_STATS);
                                } else {
                                    game.setState(GuessGameState.ROUND_GAMING);
                                    long drawerId = generateDrawer(roomId);
                                    game.setFirstGuessed(false);
                                    game.setDrawerId(drawerId);
                                    long startTime = game.getNextStartTime();
                                    game.setStartTime(startTime);
                                    game.setNextStartTime(0);
                                    game.setEndTime(startTime + ROUNG_GAME_TIME);
                                    game.setQuestion(generateQuestion());
                                    game.setRound(game.getRound() + 1);
                                    roomService.chatTo(RoomBroadcastApi.GAME_QUESTION, roomId, Arrays.asList(game.getDrawerId()), ReturnUtils.succ(game.getQuestion()));
                                    Map<String, Object> ret = new HashMap<String, Object>(getCurrentGameInfo(roomId));
                                    roomService.broadcast(RoomBroadcastApi.GAME_RUNNING, roomId, ReturnUtils.succ(ret));
                                }
                            }
                        } else if (game.getState() == GuessGameState.GAME_STATS) {
                            if (room.getStatus() == RoomStatus.GAMEING) {
                                roomService.broadcast(RoomBroadcastApi.GAME_OVER, roomId, ReturnUtils.succ(getCurrentGameInfo(roomId)));
                                roomService.setRoomState(roomId, RoomStatus.IDLE, AvatarStatus.IDLE, AvatarStatus.READY);
                                game.getTimer().cancel();
                                game.setState(GuessGameState.END);
                                roomService.broadcast(RoomBroadcastApi.ROOM_INFO, roomId, roomService.getRoomInfo(roomId));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean updateGameTime(long roomId, int minusMillis) {
        GuessGame game = gameMap.get(roomId);
        if (game != null) {
            synchronized (game) {
                long currentTime = System.currentTimeMillis();
                if (game.getEndTime() > currentTime + minusMillis) {
                    game.setEndTime(game.getEndTime() - minusMillis);
                    game.setNextStartTime(game.getNextStartTime() - minusMillis);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean startGame(long roomId, AvatarSession<Avatar> session) {
        GuessGame game = gameMap.get(roomId);
        if (game == null) {
            game = new GuessGame();
            game.setGameId(GameConst.GUESSS);
            game.setRoomId(roomId);
            gameMap.put(roomId, game);
        } else {
            game.reset();
        }
        game.setDrawerId(0);
        game.setState(GuessGameState.START);

        GameTimerTask task = new GameTimerTask(roomId);
        GameRoom room = roomService.getGameRoom(roomId);
        GuessGameInfo gameInfo = new GuessGameInfo();
        gameInfo.setCreatorId(room.getOwnerId());
        gameInfo.setCreatorName(session.getName());
        gameInfo.setInfos(new HashMap<Integer, GuessGame.GameRound>());
        if (guessGameInfoDao.insertGuessGameInfo(gameInfo)) {
            game.setGameObjId(gameInfo.getId());
            task.setGuessGameInfo(gameInfo);
            game.getTimer().scheduleAtFixedRate(task, 0, PERIOD_TIME);
            return true;
        } else {
            return false;
        }
    }

    private Guess filterAnswer(Guess guess, String answer) {
        Guess newGuess = new Guess(guess);
        String[] answerWords = answer.split("");
        for (int i = 0; i < answerWords.length; i++) {
            newGuess.setAnswer(newGuess.getAnswer().replace(answerWords[i], "*"));
        }
        return newGuess;
    }


    public Map<String, Object> getCurrentGameInfo(long roomId) {
        Map<String, Object> currentGame = new HashMap<String, Object>();
        GuessGame game = gameMap.get(roomId);
        currentGame.put("drawerId", game.getDrawerId());
        currentGame.put("round", game.getRound());
        currentGame.put("endTime", game.getEndTime());
        currentGame.put("nextStartTime", game.getNextStartTime());
        currentGame.put("scores", game.getScores());
        return currentGame;
    }

    public void answer(long roomId, AvatarSession<Avatar> avatarSession, Guess guess) {
        GuessGame game = gameMap.get(roomId);
        if (game != null) {
            synchronized (game) {
                if (isDrawer(roomId, avatarSession.getAvatarId())) {
                    avatarSession.sendMessage(ReturnUtils.failed("you are game drawer"));
                    return;
                } else {
                    Map<String, Object> ret = new HashMap<String, Object>();
                    ret.put("fAnswer", filterAnswer(guess, game.getQuestion().getAnswer()));
                    if (isCorrectAnswer(game, guess) && !hasGuessed(roomId, avatarSession.getAvatarId())) {
                        if (!game.isFirstGuessed()) {
                            addScore(GuessGame.RULE.FIRST_GUESSED, game, avatarSession.getAvatarId());
                            game.setFirstGuessed(true);
                        } else {
                            addScore(GuessGame.RULE.GUESSED, game, avatarSession.getAvatarId());
                        }
                        game.getAnswers().add(guess);
                        //答对减5s
                        updateGameTime(roomId, 5000);
                        ret.put("isCorrect", true);
                        ret.put("info", getCurrentGameInfo(roomId));
                        if (isAllGuessed(roomId)) {
                            game.setEndTime(System.currentTimeMillis());
                        }
                        roomService.broadcast(RoomBroadcastApi.GAME_ANSWER, roomId, ReturnUtils.succ(ret));
                    } else {
                        ret.put("isCorrect", false);
                        roomService.broadcast(RoomBroadcastApi.GAME_ANSWER, roomId, ReturnUtils.succ(ret));
                    }
                }
            }
        }
    }

    public ReturnUtils.GameResult like(long roomId, AvatarSession<Avatar> avatarSession) {
        GuessGame game = gameMap.get(roomId);
        if (game != null && game.getState() == GuessGameState.ROUND_INTERNAL) {
            synchronized (game) {
                if (game.getDrawerId() == avatarSession.getAvatarId()) {
                    return ReturnUtils.failed();
                }

                if (!containsRule(GuessGame.RULE.LIKE, roomId, avatarSession.getAvatarId()) && !containsRule(GuessGame.RULE.UNLIKE, roomId, avatarSession.getAvatarId())) {
                    addScore(GuessGame.RULE.LIKE, game, avatarSession.getAvatarId());
                    roomService.broadcast(RoomBroadcastApi.GAME_LIKE, roomId, ReturnUtils.succ(getCurrentGameInfo(roomId)));
                    return ReturnUtils.succ();
                }
            }
        }
        return ReturnUtils.failed();
    }

    public ReturnUtils.GameResult unlike(long roomId, AvatarSession<Avatar> avatarSession) {
        GuessGame game = gameMap.get(roomId);
        if (game != null && game.getState() == GuessGameState.ROUND_INTERNAL) {
            synchronized (game) {
                if (game.getDrawerId() == avatarSession.getAvatarId()) {
                    return ReturnUtils.failed();
                }

                if (!containsRule(GuessGame.RULE.UNLIKE, roomId, avatarSession.getAvatarId()) && !containsRule(GuessGame.RULE.LIKE, roomId, avatarSession.getAvatarId())) {
                    addScore(GuessGame.RULE.UNLIKE, game, avatarSession.getAvatarId());
                    roomService.broadcast(RoomBroadcastApi.GAME_UNLIKE, roomId, ReturnUtils.succ());
                    return ReturnUtils.succ();
                }
            }
        }
        return ReturnUtils.failed();
    }

    public ReturnUtils.GameResult exit(long roomId, AvatarSession<Avatar> avatarSession) {
        GuessGame game = gameMap.get(roomId);
        if (game != null) {
            synchronized (game) {
                if ((game.getState() != GuessGameState.END) && !containsRule(GuessGame.RULE.EXIT, roomId, avatarSession.getAvatarId())) {

                    addScore(GuessGame.RULE.EXIT, game, avatarSession.getAvatarId());
                    return ReturnUtils.succ();
                }
            }
        }
        return ReturnUtils.failed();
    }

    private void addScore(GuessGame.RULE rule, GuessGame game, long avatarId) {
        Map<Long, Map<Integer, List<GuessGame.RULE>>> operations = game.getOperations();
        Map<Long, Integer> scores = game.getScores();
        long drawerId = game.getDrawerId();
        if (!operations.containsKey(drawerId)) {
            operations.put(drawerId, new HashMap<Integer, List<GuessGame.RULE>>());
        }
        if (!operations.containsKey(avatarId)) {
            operations.put(avatarId, new HashMap<Integer, List<GuessGame.RULE>>());
        }
        if (rule == GuessGame.RULE.GUESSED || rule == GuessGame.RULE.FIRST_GUESSED || rule == GuessGame.RULE.LIKE) {
            GuessGame.RULE drawRule = null;
            if (rule == GuessGame.RULE.GUESSED || rule == GuessGame.RULE.FIRST_GUESSED) {
                drawRule = GuessGame.RULE.BE_GUESSED;
            } else if (rule == GuessGame.RULE.LIKE) {
                drawRule = GuessGame.RULE.BE_LIKED;
            }
            List<GuessGame.RULE> drawerRuleList = operations.get(drawerId).get(game.getRound());
            if (drawerRuleList == null) {
                drawerRuleList = new ArrayList<GuessGame.RULE>();
                operations.get(drawerId).put(game.getRound(), drawerRuleList);
            }
            drawerRuleList.add(drawRule);
            int drawerScore = MapUtils.getIntValue(scores, drawerId, 0) + MapUtils.getIntValue(RULE_SCORE, drawRule, 0);
            scores.put(drawerId, drawerScore > 0 ? drawerScore : 0);
            final GuessGame.RULE fdrawRule = drawRule;
            avatarSessionService.updateAvatarSession(drawerId, new AbstractAvatarSessionService.SessionCallable<Void, Avatar>() {
                @Override
                public Void call(AvatarSession<Avatar> playerSession) {
                    Avatar avatar = playerSession.getAvatar();
                    int score = avatar.getTotalScore();
                    int newScore = score + MapUtils.getIntValue(RULE_SCORE, fdrawRule, 0);
                    if(newScore < 0){
                        newScore = 0;
                    }
                    avatar.setTotalScore(newScore);
                    avatarService.save(avatar);
                    return null;
                }
            });
        }

        List<GuessGame.RULE> ruleList = operations.get(avatarId).get(game.getRound());
        if (ruleList == null) {
            ruleList = new ArrayList<GuessGame.RULE>();
            operations.get(avatarId).put(game.getRound(), ruleList);
        }
        ruleList.add(rule);
        int score = MapUtils.getIntValue(scores, avatarId, 0) + MapUtils.getIntValue(RULE_SCORE, rule, 0);
        scores.put(avatarId, score > 0 ? score : 0);
        final GuessGame.RULE fRule = rule;
        avatarSessionService.updateAvatarSession(avatarId, new AbstractAvatarSessionService.SessionCallable<Void, Avatar>() {
            @Override
            public Void call(AvatarSession<Avatar> playerSession) {
                Avatar avatar = playerSession.getAvatar();
                int score = avatar.getTotalScore();
                int newScore = score + MapUtils.getIntValue(RULE_SCORE, fRule, 0);
                if(newScore < 0){
                    newScore = 0;
                }
                avatar.setTotalScore(newScore);
                avatarService.save(avatar);
                return null;
            }
        });

    }

    public boolean containsRule(GuessGame.RULE rule, long roomId, long avatarId) {
        GuessGame game = gameMap.get(roomId);
        if (game != null) {
            Map<Long, Map<Integer, List<GuessGame.RULE>>> operations = game.getOperations();
            Map<Integer, List<GuessGame.RULE>> rules = operations.get(avatarId);
            if (MapUtils.isNotEmpty(rules)) {
                List<GuessGame.RULE> ruleList = rules.get(game.getRound());
                if (CollectionUtils.isNotEmpty(ruleList)) {
                    return ruleList.contains(rule);
                }
            }
        }
        return false;

    }

    public boolean hasGuessed(long roomId, long avatarId) {
        return containsRule(GuessGame.RULE.FIRST_GUESSED, roomId, avatarId) || containsRule(GuessGame.RULE.GUESSED, roomId, avatarId);
    }

    public boolean isAllGuessed(long roomId) {
        GameRoom room = roomService.getGameRoom(roomId);
        GuessGame game = gameMap.get(roomId);
        if (room != null && game != null) {
            for (Long avatarId : room.getSessionIds().values()) {
                if (avatarId != game.getDrawerId()) {
                    if (!hasGuessed(roomId, avatarId)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isDrawer(long roomId, long avatarId) {
        GuessGame game = gameMap.get(roomId);
        if (game != null) {
            return game.getDrawerId() == avatarId;
        } else {
            return false;
        }
    }

    private boolean isCorrectAnswer(GuessGame game, Guess guess) {
        return StringUtils.equals(game.getQuestion().getAnswer().trim(), guess.getAnswer().trim());
    }

    public List<Guess> getAnswers(long roomId) {
        return getGuessGame(roomId).getAnswers();
    }

    public GuessGameState getGuessGameState(long roomId) {
        GuessGame game = gameMap.get(roomId);
        return game.getState();
    }

    public void stopGame(long roomId) {
        GuessGame game = gameMap.get(roomId);
        if (game != null) {
            synchronized (game) {
                game.setState(GuessGameState.GAME_STATS);
            }
        }
    }

    public boolean isGameOver(GuessGame game) {
        if (game.getEndTime() <= System.currentTimeMillis()) {
            return true;
        } else {
            return false;
        }
    }

    public GuessGame getGuessGame(long roomId) {
        return ObjectUtil.deepCopy(gameMap.get(roomId));
    }


    private long generateDrawer(long roomId) {
        GameRoom room = roomService.getGameRoom(roomId);
        GuessGame game = getGuessGame(roomId);
        if (room != null && game != null) {
            Iterator<Long> iterator = room.getSessionIds().values().iterator();
            long oldDrawer = game.getDrawerId();
            if (oldDrawer == 0L) {
                return iterator.next();
            } else {
                while (iterator.hasNext()) {
                    long drawer = iterator.next();
                    if (oldDrawer == drawer) {
                        if (iterator.hasNext()) {
                            return iterator.next();
                        }
                    }
                }
            }
        }
        return 0L;
    }

    public GuessQuestion generateQuestion() {
        int random = RandomUtils.nextInt(0, questions.size());
        return questions.get(random);
    }

}
