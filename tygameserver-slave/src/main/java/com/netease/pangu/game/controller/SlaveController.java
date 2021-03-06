package com.netease.pangu.game.controller;

import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;
import com.netease.pangu.game.rpc.WsRpcCallInvoker;
import com.netease.pangu.game.util.ReturnUtils;

import javax.annotation.Resource;

@HttpController(value = "/slave", moduleId = GameConst.SYSTEM)
public class SlaveController {
    @Resource
    private WsRpcCallInvoker wsRpcCallInvoker;

    @HttpRequestMapping("/system/wsrpclist")
    public ReturnUtils.GameResult getWsRpcList(long gameId) {
        return ReturnUtils.succ(wsRpcCallInvoker.getMethodIndexMap().get(gameId));
    }

}
