package com.netease.pangu.game.meta;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.netease.pangu.game.common.meta.IAvatarGame;

@Document(collection="avatarGuessGame")
public class AvatarGuessGame implements IAvatarGame {
	private ObjectId id;
	private long gameId;
	private long avatarId;
	private long totalscore;
	private long roomId;
	private String question;
	private String answer;
	private int role;
	private long score;
	private long createTime;
	@Override
	public long getAvatarId() {
		return avatarId;
	}

	@Override
	public void setAvatarId(long avatarId) {
		this.avatarId = avatarId;
	}

	@Override
	public long getGameId() {
		return gameId;
	}

	@Override
	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	@Override
	public long getTotalScore() {
		return totalscore;
	}

	@Override
	public void setTotalScore(long score) {
		this.totalscore = score;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

}
