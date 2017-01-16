package com.netease.pangu.game.core.service;

import com.netease.pangu.game.distribution.Node;
import com.netease.pangu.game.distribution.NodeManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class NodeScheduleService {
    @Resource
    private NodeManager nodeManager;

    public Node getNodeByScheduled() {
        return DemoScheduler.getInstance().schedule(nodeManager);
    }
}
