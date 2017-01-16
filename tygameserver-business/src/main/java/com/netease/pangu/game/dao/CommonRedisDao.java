package com.netease.pangu.game.dao;

import com.netease.pangu.game.dao.redis.RedisDao;
import com.netease.pangu.game.dao.redis.RedisTemplateInject;
import org.springframework.stereotype.Component;

@Component
@RedisTemplateInject("redisTemplate")
public class CommonRedisDao extends RedisDao<String, Object> {


}
