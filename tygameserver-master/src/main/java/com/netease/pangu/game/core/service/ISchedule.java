package com.netease.pangu.game.core.service;

import com.netease.pangu.game.distribution.Slave;
import com.netease.pangu.game.distribution.SlaveManager;

public interface ISchedule {
    public Slave schedule(SlaveManager manager);
}
