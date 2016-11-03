package com.netease.pangu.game.core.service;

import com.netease.pangu.game.distribution.AppWorker;
import com.netease.pangu.game.distribution.AppWorkerManager;

public class DemoScheduler implements ISchedule {
	private final static DemoScheduler instance = new DemoScheduler();
	@Override
	public AppWorker schedule(AppWorkerManager manager) {
		AppWorker min = null;
		for(AppWorker worker : manager.getWorkersMap().values()){
			if(min == null){
				min = worker;
			}else if(min.getCount() > worker.getCount()){
				min = worker;
			}
		}		
		return min;
	}
	
	public static DemoScheduler getInstance(){
		return instance;
	}
}
