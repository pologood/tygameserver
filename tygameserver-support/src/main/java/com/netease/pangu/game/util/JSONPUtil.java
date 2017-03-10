package com.netease.pangu.game.util;

/**
 * Created by huangc on 2017/2/6.
 */
public class JSONPUtil {
    public static String getJSONP(String callback, Object obj) {
        return String.format("%s(%s)", callback, JsonUtil.toJson(obj));
    }
}
