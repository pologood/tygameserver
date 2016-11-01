package com.netease.pangu.game.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.netease.pangu.game.dao.UniqueIDGenerateDao;

@Service
public class UniqueIDGeneratorService {
	@Resource private UniqueIDGenerateDao uniqueIDGenerateDao;
	private final String PlayerID_KEY = "PlayerId";
	private final String SesssionID_KEY = "SesssionId";
	private final String RoomId_KEY = "RoomId";
	
	@PostConstruct
	public void init(){
		uniqueIDGenerateDao.getAndSetInitValue(PlayerID_KEY, 10000);
		uniqueIDGenerateDao.getAndSetInitValue(SesssionID_KEY, 10000);
		uniqueIDGenerateDao.getAndSetInitValue(RoomId_KEY, 10000);
	}
	
	public long generatePlayerId() {
		return uniqueIDGenerateDao.generate(PlayerID_KEY);
	}
	
	public long generateSessionId() {
		return uniqueIDGenerateDao.generate(SesssionID_KEY);
	}
	
	public long generateRoomId() {
		return uniqueIDGenerateDao.generate(RoomId_KEY);
	}
}
