package com.netease.pangu.game.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangc on 2017/3/8.
 */
public class BusinessCode {
    @Message("失败")
    public static final int FAILED = 0;

    @Message("成功")
    public static final int SUCC = 1;

    @Message("开启游戏失败")
    public static final int STAT_GAME_FAILED = 2;
    @Message("非法操作")
    public static final int ILLEGAL_OPERATION = 3;
    @Message("没有登陆")
    public static final int NOT_LOGINED = 4;

    @Message("已经加入房间")
    public static final int ROOM_HAS_JOINED = 10001;
    @Message("加入房间失败")
    public static final int ROOM_JOINED_FAILED = 10002;
    @Message("超过房间最大人数")
    public static final int ROOM_MAX_SIZE_EXCEED = 10003;
    @Message("创建房间失败")
    public static final int ROOM_CREATE_FAILED = 10004;

    @Message("游戏已经开始，无法删除")
    public static final int ROOM_REMOVE_GAME_RUNNING = 10005;
    @Message("房主才能删除")
    public static final int ROOM_REMOVE_ONLY_BY_OWNER = 10006;
    @Message("房间不存在")
    public static final int ROOM_NOT_EXIST = 10007;
    @Message("房间已满")
    public static final int ROOM_IS_FULL = 10008;
    @Message("加入房间状态不对")
    public static final int ROOM_IS_NOT_IN_JOIN_STATE = 10009;
    @Message("房间状态不对, 无法开启游戏")
    public static final int ROOM_STATE_NOT_SUPPORT_START = 10010;

    @Message("不在答题时间")
    public static final int GUESS_NOTIN_ANSWER_TIME = 20001;

    @Message("你是画家")
    public static final int GUESS_YOU_ARE_DRAWER = 20002;


    private static final Map<Integer, String> messageMap;

    static {
        Map<Integer, String> m = new HashMap<Integer, String>();
        for (Field field : BusinessCode.class.getDeclaredFields()) {
            Message annotation = field.getAnnotation(Message.class);
            if (annotation != null) {
                try {
                    m.put(field.getInt(null), annotation.value());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        messageMap = Collections.unmodifiableMap(m);
    }

    public static String getMessage(int errorCode) {
        return messageMap.get(errorCode);
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Message {
        String value();
    }

    public static ReturnUtils.GameResult failed(int code) {
        return ReturnUtils.failed(code, BusinessCode.getMessage(code));
    }

    public static ReturnUtils.GameResult failed() {
        return ReturnUtils.failed(BusinessCode.FAILED, BusinessCode.getMessage(BusinessCode.FAILED));
    }
}
