package com.netease.pangu.game.dao;

import com.netease.pangu.game.dao.redis.RedisDao;
import com.netease.pangu.game.dao.redis.RedisTemplateInject;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RedisTemplateInject("redisTemplate")
public class CommonRedisDao extends RedisDao<String, Object> {
    public void putWithTTL(String key, Object value, long time, TimeUnit timeUnit) {
        super.put(key, value, time, timeUnit);
    }
}
