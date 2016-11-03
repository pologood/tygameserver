package com.netease.pangu.game.demo.business.controller.http;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.netease.pangu.game.core.service.AppWorkerScheduleService;
import com.netease.pangu.game.distribution.AppWorker;
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;
import com.netease.pangu.game.meta.Player;
import com.netease.pangu.game.service.PlayerManager;

@HttpController("/master")
public class MasterController {
	@Resource AppWorkerScheduleService appWorkerScheduleService;
	@Resource PlayerManager playerManager;
	
	@HttpRequestMapping("/app")
	public Map<String,Object> getAppWorker(String playerUUID){
		AppWorker worker = appWorkerScheduleService.getWorkerByScheduled();
		if(worker != null){
			Map<String, Object> workerInfo = new HashMap<String, Object>();
			workerInfo.put("ip", worker.getIp());
			workerInfo.put("port", worker.getPort());
			return workerInfo;
		}
		return null;
	}
	
	@HttpRequestMapping("/player")
	public Map<String, Object> getPlayerByUUID(String uuid){
		Player player = playerManager.getPlayerByUUID(uuid);
		Map<String, Object> playerObj = new HashMap<String, Object>();
		playerObj.put("name", player.getName());
		playerObj.put("uuid", player.getUuid());
		playerObj.put("pId", player.getPid());
		playerObj.put("serverAttrs", player.getServerAttrs());
		return playerObj;
	}
}
