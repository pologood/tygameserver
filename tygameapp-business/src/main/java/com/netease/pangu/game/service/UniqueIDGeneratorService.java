package com.netease.pangu.game.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class UniqueIDGeneratorService {
	
	private final AtomicLong ID = new AtomicLong(0);
	
	public long generate() {
		return ID.incrementAndGet();
	}
	
}
