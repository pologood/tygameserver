package com.netease.pangu.game.core.service;

import com.netease.pangu.game.distribution.AppWorker;
import com.netease.pangu.game.distribution.AppWorkerManager;

public interface ISchedule {
	public AppWorker schedule(AppWorkerManager manager);
}
