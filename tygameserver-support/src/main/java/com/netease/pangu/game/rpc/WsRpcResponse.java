package com.netease.pangu.game.rpc;

import java.util.HashMap;
import java.util.Map;

public class WsRpcResponse {
    private String rpcMethod;
    private Map<String, Object> attr;
    private Object content;

    public static WsRpcResponse create(String rpcMethodName) {
        return new WsRpcResponse(rpcMethodName);
    }

    public static WsRpcResponse create(String rpcMethodName, Object content) {
        WsRpcResponse response = new WsRpcResponse(rpcMethodName);
        response.setContent(content);
        return response;
    }

    public WsRpcResponse(String rpcMethodName) {
        this.rpcMethod = rpcMethodName;
        attr = new HashMap<String, Object>();
    }

    public WsRpcResponse() {
        attr = new HashMap<String, Object>();
    }

    public String getRpcMethod() {
        return rpcMethod;
    }

    public void setRpcMethod(String rpcMethod) {
        this.rpcMethod = rpcMethod;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Object getAttr(String key) {
        return attr.get(key);
    }

    public void setAttr(String key, Object value) {
        attr.put(key, value);
    }
}
