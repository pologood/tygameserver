package com.netease.pangu.game.meta;

import java.util.Map;

public class GuessGame {
	public static class Question{
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
	public static class Guess{
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
	
	public static final int START = 1;
	public static final int UNREADY = -1;
	public static final int READY = 0;
	
	private long gameId;
	private long roomId;
	private long startTime;
	private long endTime;
	private long drawerId;
	private Question question;	
	private Map<Long, Guess> answers;
	private int state;

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
	public Map<Long, Guess> getAnswers() {
		return answers;
	}
	public void setAnswers(Map<Long, Guess> answers) {
		this.answers = answers;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	
}
