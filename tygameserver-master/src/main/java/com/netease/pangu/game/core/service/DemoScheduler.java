package com.netease.pangu.game.core.service;

import com.netease.pangu.game.distribution.Slave;
import com.netease.pangu.game.distribution.SlaveManager;

public class DemoScheduler implements ISchedule {
    private final static DemoScheduler instance = new DemoScheduler();

    @Override
    public Slave schedule(SlaveManager manager) {
        Slave min = null;
        for (Slave worker : manager.getWorkersMap().values()) {
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
