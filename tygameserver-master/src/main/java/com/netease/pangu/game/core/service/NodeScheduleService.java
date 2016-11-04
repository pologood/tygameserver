package com.netease.pangu.game.core.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.netease.pangu.game.distribution.Node;
import com.netease.pangu.game.distribution.NodeManager;

@Service
public class NodeScheduleService {
	@Resource private NodeManager nodeManager;
	
	public Node getNodeByScheduled(){
		return DemoScheduler.getInstance().schedule(nodeManager);
	}
}
