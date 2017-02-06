package com.netease.pangu.game.controller;

import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;

/**
 * Created by huangc on 2017/2/6.
 */

@HttpController(value = "/auth", gameId = GameConst.SYSTEM)
public class AuthController {
    @HttpRequestMapping("/token")
    public String getToken(String gbId){
        return gbId;
    }

}
