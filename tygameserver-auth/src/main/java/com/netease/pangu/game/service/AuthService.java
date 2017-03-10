package com.netease.pangu.game.service;

import com.netease.pangu.game.common.dao.CommonRedisDao;
import com.netease.pangu.game.util.ReturnUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangc on 2017/2/6.
 */
@Service
public class AuthService {
    private static String SALT_SEGMENT = "U2FsdGVkX1+UnlCdvWjG6WMPaER1ZEdgRYUUDOSHcxOaKLysVhVeGTiatNRH0Pj3";
    @Resource
    private CommonRedisDao commonRedisDao;

    public String generateToken(String uuid, long expireTime) {
        String authCode = DigestUtils.md5Hex(uuid + expireTime + SALT_SEGMENT);
        commonRedisDao.putWithTTL(authCode, expireTime, 10 * 60 * 1000, TimeUnit.MILLISECONDS);
        return authCode;
    }

    public ReturnUtils.GameResult checkToken(String authCode) {
        Long expireTime = (Long) commonRedisDao.get(authCode);
        if (expireTime != null) {
            if (expireTime > System.currentTimeMillis()) {
                commonRedisDao.expire(authCode, 5, TimeUnit.SECONDS);
                return ReturnUtils.succ();
            }
        }
        return ReturnUtils.failed(ReturnUtils.FAILED, null);
    }
}
