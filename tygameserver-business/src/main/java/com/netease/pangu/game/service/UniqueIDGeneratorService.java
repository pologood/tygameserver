package com.netease.pangu.game.service;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.netease.pangu.game.dao.UniqueIDGenerateDao;

@Service
public class UniqueIDGeneratorService {
	@Resource private UniqueIDGenerateDao uniqueIDGenerateDao;
	private final String PlayerID_KEY = "PlayerId";
	private final AtomicLong SessionID = new AtomicLong(10000);
	private final AtomicLong RoomID = new AtomicLong(1);
	@PostConstruct
	public void init(){
		uniqueIDGenerateDao.getAndSetInitValue(PlayerID_KEY, 10000);
	}
	
	public long generatePlayerId() {
		return uniqueIDGenerateDao.generate(PlayerID_KEY);
	}
	
	public long generateSessionId() {
		return SessionID.getAndIncrement();
	}
	
	public long generateRoomId() {
		return RoomID.getAndIncrement();
	}
}
