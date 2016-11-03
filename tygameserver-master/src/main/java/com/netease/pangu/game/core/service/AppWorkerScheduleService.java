package com.netease.pangu.game.core.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.netease.pangu.game.distribution.AppWorker;
import com.netease.pangu.game.distribution.AppWorkerManager;

@Service
public class AppWorkerScheduleService {
	@Resource private AppWorkerManager appWorkManager;
	
	public AppWorker getWorkerByScheduled(){
		return DemoScheduler.getInstance().schedule(appWorkManager);
	}
}
