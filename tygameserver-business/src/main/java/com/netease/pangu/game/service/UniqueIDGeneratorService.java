package com.netease.pangu.game.service;

import com.netease.pangu.game.dao.UniqueIDGenerateDao;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UniqueIDGeneratorService {
    @Resource
    private UniqueIDGenerateDao uniqueIDGenerateDao;
    private final String PlayerID_KEY = "PlayerId";
    private final AtomicLong SessionID = new AtomicLong(10000);

    @PostConstruct
    public void init() {
        uniqueIDGenerateDao.getAndSetInitValue(PlayerID_KEY, 10000);
    }

    public long generateAvatarId() {
        return uniqueIDGenerateDao.generate(PlayerID_KEY);
    }

    public long generateSessionId() {
        return SessionID.getAndIncrement();
    }
}
