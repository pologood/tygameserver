package com.netease.pangu.game.dao;

import com.google.common.reflect.TypeToken;
import com.netease.pangu.game.meta.DataCenterSimpleRoleInfo;
import com.netease.pangu.game.util.HttpClientUtils;
import com.netease.pangu.game.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangc on 2017/1/24.
 */
@Component
public class DataCenterApiDao {
    private @Value("${datacenter.url:}") String ApiURL;
    private static final String SIMPLE_BYURS_API = "/api/simpleAvatar/by-urs";

    private String getData(String url, Map<String, Object> params) {
        HttpClientUtils.HttpResult result = HttpClientUtils.get(ApiURL + url, params);
        if (result == null || result.getStatusCode() < 200 || result.getStatusCode() > 299 || StringUtils.equalsIgnoreCase(result.getContentAsString(), "null")) {
            return null;
        }
        return result.getContentAsString();
    }

    public Map<String, List<DataCenterSimpleRoleInfo>> getSimpleAvatarsByUrs(String urs) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("urs", urs);
        String data = getData(SIMPLE_BYURS_API, params);
        return data != null
                ? JsonUtil.fromJson(data, new TypeToken<Map<String, List<DataCenterSimpleRoleInfo>>>(){}): null;
    }
}
