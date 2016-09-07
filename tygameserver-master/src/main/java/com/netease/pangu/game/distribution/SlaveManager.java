package com.netease.pangu.game.distribution;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

@Component
public class SlaveManager {
	private ConcurrentMap<String, Worker> appNodes = new ConcurrentHashMap<>();

	public static String getSlaveKey(Worker node){
		return String.format("%s:%d", node.getIp(), node.getPort());
	}
	
	public boolean addNode(Worker node) {
		return appNodes.putIfAbsent(getSlaveKey(node), node) == null ? true : false;
	}
	
	public boolean removeNode(Worker node){
		return appNodes.remove(getSlaveKey(node)) != null;
	}
}
