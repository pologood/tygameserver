package com.netease.pangu.game.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.Map;

public class JsonUtil {
    private final static Gson gson = new Gson();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    @SuppressWarnings("serial")
    public static Map<String, Object> fromJson(String jsonStr) {
        return gson.fromJson(jsonStr, (new TypeToken<Map<String, Object>>() {
        }).getType());
    }

    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        return gson.fromJson(jsonStr, clazz);

    }
}
