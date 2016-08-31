package com.netease.pangu.game.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class UniqueIDGeneratorService {
	
	private final AtomicLong PlayerID = new AtomicLong(100000);
	private final AtomicLong SesssionID = new AtomicLong(10000);
	private final AtomicLong RoomId = new AtomicLong(10000);
	
	public long generatePlayerId() {
		return PlayerID.incrementAndGet();
	}
	
	public long generateSessionId() {
		return SesssionID.incrementAndGet();
	}
	
	public long generateRoomId() {
		return RoomId.incrementAndGet();
	}
}
