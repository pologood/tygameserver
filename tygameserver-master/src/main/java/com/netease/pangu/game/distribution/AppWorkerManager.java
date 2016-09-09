package com.netease.pangu.game.distribution;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

@Component
public class AppWorkerManager {
	private ConcurrentMap<String, AppWorker> appNodes = new ConcurrentHashMap<>();

	public static String getKey(AppWorker worker){
		return String.format("%s:%d", worker.getIp(), worker.getPort());
	}
	
	public Map<String, AppWorker> getWorkersMap(){
		return Collections.unmodifiableMap(appNodes);
	}
	
	public boolean addNode(AppWorker worker) {
		return appNodes.putIfAbsent(getKey(worker), worker) == null;
	}
	
	public boolean removeNode(AppWorker worker){
		return appNodes.remove(getKey(worker)) != null;
	}
}
