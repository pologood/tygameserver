package com.netease.pangu.game.core.service;

import com.netease.pangu.game.distribution.Node;
import com.netease.pangu.game.distribution.NodeManager;

public interface ISchedule {
    public Node schedule(NodeManager manager);
}
