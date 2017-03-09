package com.netease.pangu.game.util;

public class ReturnUtils {
    public final static int SUCC = 1;
    public final static int FAILED = 0;

    public static GameResult succ() {
        return succ(null, null, null);
    }

    public static GameResult succ(String message) {
        return succ(null, null, message);
    }

    public static GameResult succ(Object payload) {
        return succ(payload, null, null);
    }

    public static GameResult succ(Object payload, String message) {
        return succ(payload, null, message);
    }

    public static GameResult succ(Object payload, Object source) {
        return succ(payload, source, null);
    }

    public static GameResult succ(Object payload, Object source, String message) {
        GameResult result = new GameResult();
        result.setCode(SUCC);
        result.setPayload(payload);
        result.setSource(source);
        result.setMessage(message);
        return result;
    }

    public static GameResult failed(int code, Object payload, Object source, String message) {
        GameResult result = new GameResult();
        result.setCode(code);
        result.setPayload(payload);
        result.setSource(source);
        result.setMessage(message);
        return result;
    }

    public static GameResult failed(int code, Object payload, String message) {
        return failed(code, payload, null, message);
    }

    public static GameResult failed(int code, String message) {
        return failed(code, null, null, message);
    }

    public static class GameResult {
        private int code;
        private Object payload;
        private Object source;
        private String message;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public Object getPayload() {
            return payload;
        }

        public void setPayload(Object payload) {
            this.payload = payload;
        }

        public Object getSource() {
            return source;
        }

        public void setSource(Object source) {
            this.source = source;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }

}
