package com.netease.pangu.game.service;

import com.netease.pangu.game.common.service.GlobalValueCacheService;
import com.netease.pangu.game.dao.DataCenterApiDao;
import com.netease.pangu.game.meta.DataCenterSimpleRoleInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by huangc on 2017/1/24.
 */
@Service
public class DataCenterApiService {
    private static final int EXPIRE_TIME = 5*60*1000; //5分钟
    @Resource
    private DataCenterApiDao dataCenterApiDao;
    @Resource
    private GlobalValueCacheService globalValueCacheService;

    private static final String SIMPLE_AVATAR_BYURS_KEY_PREFIX = "DC_SABYURS_KP";
    private static final String GAME_SERVERS_KEY_PREFIX = "DC_GAMESERVER_KP";
    public static String getUpdateKey(String keyPrefix, String key) {
        return String.format("%s-%s", keyPrefix, key);
    }

    /**
     * 获取玩家角色基本信息
     * @param urs
     * @return
     */
    public Map<String, List<DataCenterSimpleRoleInfo>> getSimpleAvatarsInfoByUrs(final String urs){
        final String avatarByUrsKey = getUpdateKey(SIMPLE_AVATAR_BYURS_KEY_PREFIX, urs);
        return globalValueCacheService.doPersistentCachedWithTTLForUpdate(new GlobalValueCacheService.CacheableValueWithTTLForUpdate<Map<String, List<DataCenterSimpleRoleInfo>>>() {
            @Override
            public Map<String, List<DataCenterSimpleRoleInfo>> getMissValue() {
                return dataCenterApiDao.getSimpleAvatarsByUrs(urs);
            }

            @Override
            public String getCachedKey() {
                return avatarByUrsKey;
            }

            @Override
            public long getTTL() {
                return EXPIRE_TIME;
            }
        });
    }

    public Map<String, Object> getGameServers(){
        return globalValueCacheService.doPersistentCachedWithTTLForUpdate(new GlobalValueCacheService.CacheableValueWithTTLForUpdate<Map<String, Object>>() {
            @Override
            public Map<String, Object> getMissValue() {
                return dataCenterApiDao.getGameServers();
            }

            @Override
            public String getCachedKey() {
                return GAME_SERVERS_KEY_PREFIX;
            }

            @Override
            public long getTTL() {
                return EXPIRE_TIME;
            }
        });
    }
}
