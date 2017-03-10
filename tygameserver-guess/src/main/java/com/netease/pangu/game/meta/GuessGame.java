package com.netease.pangu.game.meta;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class GuessGame {
    private ObjectId gameObjId;
    private long gameId;
    private long roomId;
    private long startTime;
    private long endTime;
    private long nextStartTime;
    private long drawerId;
    private GuessQuestion question;
    private List<Guess> answers;
    private boolean isFirstGuessed;
    private Map<Long, Integer> scores;

    private Map<Long, Map<Integer, List<RULE>>> operations;
    private GuessGameState state;
    private int round;


    @Transient
    private java.util.Timer timer;

    public GuessGame() {
        this.answers = new ArrayList<Guess>();
        this.operations = new HashMap<Long, Map<Integer, List<RULE>>>();
        this.scores = new HashMap<Long, Integer>();
        this.timer = new Timer();
    }

    public void reset() {
        this.answers = new ArrayList<Guess>();
        this.operations = new HashMap<Long, Map<Integer, List<RULE>>>();
        this.scores = new HashMap<Long, Integer>();
        this.timer = new Timer();
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDrawerId() {
        return drawerId;
    }

    public void setDrawerId(long drawerId) {
        this.drawerId = drawerId;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public GuessQuestion getQuestion() {
        return question;
    }

    public void setQuestion(GuessQuestion question) {
        this.question = question;
    }

    public List<Guess> getAnswers() {
        return answers;
    }

    public void addAnswer(Guess guess) {
        this.answers.add(guess);
    }

    public GuessGameState getState() {
        return state;
    }

    public void setState(GuessGameState state) {
        this.state = state;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public Map<Long, Map<Integer, List<RULE>>> getOperations() {
        return operations;
    }

    public Map<Long, Integer> getScores() {
        return scores;
    }

    public boolean isFirstGuessed() {
        return isFirstGuessed;
    }

    public void setFirstGuessed(boolean firstGuessed) {
        isFirstGuessed = firstGuessed;
    }

    public Timer getTimer() {
        return timer;
    }

    public long getNextStartTime() {
        return nextStartTime;
    }

    public void setNextStartTime(long nextStartTime) {
        this.nextStartTime = nextStartTime;
    }

    public ObjectId getGameObjId() {
        return gameObjId;
    }

    public void setGameObjId(ObjectId gameObjId) {
        this.gameObjId = gameObjId;
    }

    public enum RULE {
        FIRST_GUESSED(1, "猜题人：首个猜到答案"),
        GUESSED(2, "猜题人：猜到答案"),
        LIKE(3, "画题人：绘画被点赞"),
        UNLIKE(4, "不喜欢"),
        BE_GUESSED(5, "画题人：每次作品被别人"),
        EXIT(6, "中途离开"),
        BE_LIKED(7, "画题人：绘画被点赞"),
        BE_UNLIKED(8, "画题人：绘画被点赞");

        private int id;
        private String desc;

        private RULE(int id, String desc) {
            this.id = id;
            this.desc = desc;
        }
    }

    public static class Guess {
        private long avatarId;
        private String avatarName;
        private String answer;
        private long time;

        public Guess() {

        }

        public Guess(Guess guess) {
            this.setAnswer(guess.getAnswer());
            this.setAvatarId(guess.getAvatarId());
            this.setTime(guess.getTime());
            this.setAvatarName(guess.getAvatarName());
        }

        public long getAvatarId() {
            return avatarId;
        }

        public void setAvatarId(long avatarId) {
            this.avatarId = avatarId;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getAvatarName() {
            return avatarName;
        }

        public void setAvatarName(String avatarName) {
            this.avatarName = avatarName;
        }
    }

    public static class GameRound {
        private int round;
        private long ownerId;
        private long startTime;
        private long endTime;
        private long nextStartTime;
        private long drawerId;
        private GuessQuestion question;
        private List<Guess> answers;
        private boolean isFirstGuessed;
        private Map<Long, Integer> scores;
        private Map<Long, Map<Integer, List<RULE>>> operations;

        public GameRound(GuessGame game, long ownerId) {
            this.setOwnerId(ownerId);
            this.setRound(game.getRound());
            this.setStartTime(game.getStartTime());
            this.setEndTime(game.getEndTime());
            this.setNextStartTime(game.getNextStartTime());
            this.setDrawerId(game.getDrawerId());
            this.setQuestion(game.getQuestion());
            this.setAnswers(game.getAnswers());
            this.setFirstGuessed(game.isFirstGuessed());
            this.setScores(game.getScores());
            this.setOperations(game.getOperations());
        }

        public long getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(long ownerId) {
            this.ownerId = ownerId;
        }


        public int getRound() {
            return round;
        }

        public void setRound(int round) {
            this.round = round;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public long getNextStartTime() {
            return nextStartTime;
        }

        public void setNextStartTime(long nextStartTime) {
            this.nextStartTime = nextStartTime;
        }

        public long getDrawerId() {
            return drawerId;
        }

        public void setDrawerId(long drawerId) {
            this.drawerId = drawerId;
        }

        public GuessQuestion getQuestion() {
            return question;
        }

        public void setQuestion(GuessQuestion question) {
            this.question = question;
        }

        public List<Guess> getAnswers() {
            return answers;
        }

        public void setAnswers(List<Guess> answers) {
            this.answers = answers;
        }

        public boolean isFirstGuessed() {
            return isFirstGuessed;
        }

        public void setFirstGuessed(boolean firstGuessed) {
            isFirstGuessed = firstGuessed;
        }

        public Map<Long, Integer> getScores() {
            return scores;
        }

        public void setScores(Map<Long, Integer> scores) {
            this.scores = scores;
        }

        public Map<Long, Map<Integer, List<RULE>>> getOperations() {
            return operations;
        }

        public void setOperations(Map<Long, Map<Integer, List<RULE>>> operations) {
            this.operations = operations;
        }
    }
}
