package com.netease.pangu.game.service;

/**
 * Created by huangc on 2017/2/10.
 */
public class RoomBroadcastApi{
    public static final String ROOM_BROADCAST = "/room/broadcast/";
    public static final String ROOM_PRIVATE = "/room/private/";

    public static final String ROOM_INFO = "info";
    public static final String ROOM_READY = "ready";
    public static final String ROOM_CHANGE_OWNER = "changeowner";
    public static final String ROOM_JOIN = "join";
    public static final String ROOM_EXIT = "exit";
    public static final String ROOM_REMOVE = "remove";


    public final static String GAME_START = "guess/start";
    public final static String GAME_OVER = "guess/gameover";
    public final static String GAME_ROUND_OVER = "guess/roundover";
    public final static String GAME_RUNNING = "guess/running";
    public final static String GAME_QUESTION= "guess/quesion";
    public final static String GAME_ANSWER = "guess/answer";
    public final static String GAME_LIKE = "guess/like";
    public final static String GAME_UNLIKE = "guess/unlike";
    public final static String GAME_EXIT = "guess/exit";
    public final static String GAME_HINT1 = "guess/hint1";
    public final static String GAME_HINT2 = "guess/hint2";
    public final static String GAME_COUNTDOWN = "guess/countdown";
    public final static String GAME_INTERVAL_COUNTDOWN = "guess/incountdown";
}
