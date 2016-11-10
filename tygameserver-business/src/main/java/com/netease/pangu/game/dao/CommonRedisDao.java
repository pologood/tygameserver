package com.netease.pangu.game.dao;

import org.springframework.stereotype.Component;

import com.netease.pangu.game.dao.redis.RedisDao;
import com.netease.pangu.game.dao.redis.RedisTemplateInject;

@Component
@RedisTemplateInject("redisTemplate")
public class CommonRedisDao extends RedisDao<String, Object>{


}
