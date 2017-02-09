package com.netease.pangu.game.service;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameRoom;
import com.netease.pangu.game.dao.impl.GuessGameInfoDaoImpl;
import com.netease.pangu.game.meta.*;
import com.netease.pangu.game.meta.GuessGame.Guess;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GuessGameService {
    private final ConcurrentMap<Long, GuessGame> gameMap = new ConcurrentHashMap<Long, GuessGame>();

    @Resource
    private RoomService roomService;

    @Resource
    GuessGameInfoDaoImpl guessGameInfoDao;

    private final List<GuessQuestion> questions = new ArrayList<GuessQuestion>();

    public final static long gameId = 1;
    private final static int TOTOAL_ROUND = 8;
    private final static int ROUND_INTERVAL_TIME = 5000;
    private final static int ROUNG_GAME_TIME = 60000;
    private final static int PERIOD_TIME = 10;
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
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    };

    public void stop(){
        checkTimer.cancel();
    }

    static {
        RULE_SCORE = new HashMap<GuessGame.RULE, Integer>();
        RULE_SCORE.put(GuessGame.RULE.FIRST_GUESSED, 3);
        RULE_SCORE.put(GuessGame.RULE.GUESSED, 1);
        RULE_SCORE.put(GuessGame.RULE.BE_GUESSED, 1);
        RULE_SCORE.put(GuessGame.RULE.LIKE, 1);
        RULE_SCORE.put(GuessGame.RULE.UNLIKE, 0);
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
            checkTimer.scheduleAtFixedRate(checkGameStateTask, 0, PERIOD_TIME);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private final static String GAME_START = "guess/start";
    private final static String GAME_OVER = "guess/gameover";
    private final static String GAME_ROUND_OVER = "guess/roundover";
    private final static String GAME_RUNNING = "guess/running";
    private final static String GAME_QUESTION= "guess/quesion";
    private final static String GAME_ANSWER = "guess/answer";
    private final static String GAME_LIKE = "guess/like";
    private final static String GAME_UNLIKE = "guess/unlike";
    private final static String GAME_EXIT = "guess/exit";
    private final static String GAME_HINT1 = "guess/hint1";
    private final static String GAME_HINT2 = "guess/hint2";

    public static boolean isNearEqual(long t1, long t2){
        return t1 >= t2 && t1 < t2 + PERIOD_TIME;
    }

    public class GameTimerTask extends TimerTask {

        private long roomId;
        private GuessGameInfo guessGameInfo;

        public  GameTimerTask(long roomId){
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
                if (game != null) {
                    synchronized (game) {
                        long current = System.currentTimeMillis();

                        if (game.getState() == GuessGameState.START) {
                            game.setDrawerId(generateDrawer(roomId));
                            game.setStartTime(current);
                            game.setNextStartTime(0);
                            game.setEndTime(current + ROUNG_GAME_TIME);
                            game.setRound(1);
                            game.setState(GuessGameState.ROUND_GAMING);
                            game.setQuestion(generateQuestion());
                            roomService.chatTo(GAME_QUESTION, roomId, Arrays.asList(game.getDrawerId()), ReturnUtils.succ(game.getQuestion()));
                            roomService.broadcast(GAME_START, roomId, ReturnUtils.succ(getCurrentGameInfo(roomId)));
                        } else if (game.getState() == GuessGameState.ROUND_GAMING) {
                            if (current < game.getEndTime()) {
                                if (isNearEqual(current, game.getStartTime() + 5000)){
                                    roomService.broadcast(GAME_HINT1, roomId, ReturnUtils.succ(game.getQuestion().getHint1()));
                                }

                                if (isNearEqual(current, game.getStartTime() + 15000)) {
                                    roomService.broadcast(GAME_HINT2, roomId, ReturnUtils.succ(game.getQuestion().getHint2()));
                                }
                            } else {
                                if (game.getRound() == TOTOAL_ROUND) {
                                    game.setState(GuessGameState.GAME_STATS);
                                    roomService.broadcast(GAME_OVER, roomId, ReturnUtils.succ(getCurrentGameInfo(roomId)));
                                } else {
                                    game.setState(GuessGameState.ROUND_INTERNAL);
                                    long nextStartTime = current + ROUND_INTERVAL_TIME;
                                    game.setNextStartTime(nextStartTime);
                                    Map<String, Object> ret = new HashMap<String, Object>(getCurrentGameInfo(roomId));
                                    ret.put("answer", game.getQuestion().getAnswer());
                                    roomService.broadcast(GAME_ROUND_OVER, roomId, ReturnUtils.succ(ret));
                                }
                                getGuessGameInfo().getInfos().put(game.getRound(), new GuessGame.GameRound(game, roomService.getGameRoom(roomId).getOwnerId()));
                                guessGameInfoDao.save(getGuessGameInfo());
                            }
                        } else if (game.getState() == GuessGameState.ROUND_INTERNAL && game.getRound() < TOTOAL_ROUND) {
                            if (isNearEqual(current, game.getNextStartTime())) {
                                game.setState(GuessGameState.ROUND_GAMING);
                                long drawerId = generateDrawer(roomId);
                                game.setDrawerId(drawerId);
                                game.setStartTime(game.getNextStartTime());
                                game.setNextStartTime(0);
                                game.setEndTime(game.getNextStartTime() + ROUNG_GAME_TIME);
                                game.setQuestion(generateQuestion());
                                game.setRound(game.getRound() + 1);
                                roomService.chatTo(GAME_QUESTION, roomId, Arrays.asList(game.getDrawerId()), ReturnUtils.succ(game.getQuestion()));
                                Map<String, Object> ret = new HashMap<String, Object>(getCurrentGameInfo(roomId));
                                roomService.broadcast(GAME_RUNNING, roomId, ReturnUtils.succ(ret));
                            }
                        } else if (game.getState() == GuessGameState.GAME_STATS) {

                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public boolean updateGameTime(long roomId, int minusMillis){
        GuessGame game = gameMap.get(roomId);
        if(game != null) {
            synchronized (game) {
                long currentTime = System.currentTimeMillis();
                if(game.getEndTime() > currentTime + minusMillis){
                    game.setEndTime(game.getEndTime() - minusMillis);
                    game.setNextStartTime(game.getNextStartTime() - minusMillis);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean startGame(long roomId, AvatarSession<Avatar> session) {
        if (!gameMap.containsKey(roomId)) {
            GuessGame game = new GuessGame();
            game.setGameId(gameId);
            game.setRoomId(roomId);
            game.setRound(0);
            game.setDrawerId(0);
            game.setState(GuessGameState.START);
            GameTimerTask task = new GameTimerTask(roomId);
            if (gameMap.putIfAbsent(roomId, game) == null) {
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
                }
            }
        }
        return false;
    }

    private Guess filterAnswer(Guess guess){
        return guess;
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
                    Map<String, Object> ret = new HashMap<String, Object>(getCurrentGameInfo(roomId));
                    ret.put("fAnswer", filterAnswer(guess));
                    if (isCorrectAnswer(game, guess)) {
                        if (!game.isFirstGuessed()) {
                            addScore(GuessGame.RULE.FIRST_GUESSED, game, avatarSession.getAvatarId());
                            game.setFirstGuessed(true);
                        }else{
                            addScore(GuessGame.RULE.GUESSED, game, avatarSession.getAvatarId());
                        }
                        game.getAnswers().add(guess);
                        //答对减5s
                        updateGameTime(roomId, 5000);
                        ret.put("isCorrect",  true);
                        ret.put("info", getCurrentGameInfo(roomId));
                        roomService.broadcast(GAME_ANSWER, roomId, ReturnUtils.succ(ret));
                    } else{
                        ret.put("isCorrect",  false);
                        roomService.broadcast(GAME_ANSWER, roomId, ReturnUtils.succ(ret));
                    }
                }
            }
        }
    }

    public ReturnUtils.GameResult like(long roomId, AvatarSession<Avatar> avatarSession){
        GuessGame game = gameMap.get(roomId);
        if (game != null && game.getState() == GuessGameState.ROUND_INTERNAL) {
            synchronized (game) {
                if(!containsRule(GuessGame.RULE.LIKE, roomId, avatarSession.getAvatarId())){
                    addScore(GuessGame.RULE.LIKE, game, avatarSession.getAvatarId());
                    roomService.broadcast(GAME_LIKE, roomId, ReturnUtils.succ());
                    return ReturnUtils.succ();
                }
            }
        }
        return ReturnUtils.failed();
    }

    public ReturnUtils.GameResult unlike(long roomId, AvatarSession<Avatar> avatarSession){
        GuessGame game = gameMap.get(roomId);
        if (game != null && game.getState() == GuessGameState.ROUND_INTERNAL) {
            synchronized (game) {
                if(!containsRule(GuessGame.RULE.UNLIKE, roomId, avatarSession.getAvatarId())){
                    addScore(GuessGame.RULE.UNLIKE, game, avatarSession.getAvatarId());
                    roomService.broadcast(GAME_UNLIKE, roomId, ReturnUtils.succ());
                    return ReturnUtils.succ();
                }
            }
        }
        return ReturnUtils.failed();
    }

    public ReturnUtils.GameResult exit(long roomId, AvatarSession<Avatar> avatarSession){
        GuessGame game = gameMap.get(roomId);
        if(game != null){
            synchronized (game){
                if(!containsRule(GuessGame.RULE.EXIT, roomId, avatarSession.getAvatarId())){
                    addScore(GuessGame.RULE.EXIT, game, avatarSession.getAvatarId());
                    if(roomService.exitRoom(avatarSession.getAvatarId())) {
                        roomService.broadcast(GAME_EXIT, roomId, ReturnUtils.succ(avatarSession.getAvatarId()));
                        return ReturnUtils.succ();
                    }
                }
            }
        }
        return ReturnUtils.failed();
    }

    private void addScore(GuessGame.RULE rule,  GuessGame game, long avatarId) {
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

    public boolean isDrawer(long roomId, long avatarId) {
        GuessGame game = gameMap.get(roomId);
        if(game != null) {
            return game.getDrawerId() == avatarId;
        }else{
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

    public void stopGame(long roomId){
        GuessGame game = gameMap.get(roomId);
        if(game != null) {
            synchronized (game) {
                gameMap.remove(roomId);
                game.getTimer().cancel();
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
        Long[] avatarIds = room.getSessionIds().toArray(new Long[0]);
        int random = RandomUtils.nextInt(0, avatarIds.length);
        return avatarIds[random];
    }

    public GuessQuestion generateQuestion() {
        int random = RandomUtils.nextInt(0, questions.size());
        return questions.get(random);
    }

}
