package com.netease.pangu.game.meta;

import java.util.ArrayList;
import java.util.List;

public class GuessGame {
    private long gameId;
    private long roomId;
    private long startTime;
    private long endTime;
    private long drawerId;
    private Question question;
    private List<Guess> answers;
    private int state;
    private int round;
    public final static int QUESTION_CHOOSING = 0;
    public final static int QUESTION_OK = 1;

    public GuessGame() {
        this.answers = new ArrayList<Guess>();
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

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public List<Guess> getAnswers() {
        return answers;
    }

    public void addAnswer(Guess guess) {
        this.answers.add(guess);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public static class Question {
        private long avatarId;
        private String hint1;
        private String hint2;
        private String answer;
        private Object draw;

        public long getAvatarId() {
            return avatarId;
        }

        public void setAvatarId(long avatarId) {
            this.avatarId = avatarId;
        }

        public String getHint1() {
            return hint1;
        }

        public void setHint1(String hint1) {
            this.hint1 = hint1;
        }

        public String getHint2() {
            return hint2;
        }

        public void setHint2(String hint2) {
            this.hint2 = hint2;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public Object getDraw() {
            return draw;
        }

        public void setDraw(Object draw) {
            this.draw = draw;
        }
    }

    public static class Guess {
        private long avatarId;
        private String answer;
        private long time;

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
    }
}
