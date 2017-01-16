package com.netease.pangu.game.core.service;

import com.netease.pangu.game.distribution.Node;
import com.netease.pangu.game.distribution.NodeManager;

public class DemoScheduler implements ISchedule {
    private final static DemoScheduler instance = new DemoScheduler();

    @Override
    public Node schedule(NodeManager manager) {
        Node min = null;
        for (Node worker : manager.getWorkersMap().values()) {
            if (min == null) {
                min = worker;
            } else if (min.getCount() > worker.getCount()) {
                min = worker;
            }
        }
        return min;
    }

    public static DemoScheduler getInstance() {
        return instance;
    }
}
