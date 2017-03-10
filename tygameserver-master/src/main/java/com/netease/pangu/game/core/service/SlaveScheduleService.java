package com.netease.pangu.game.core.service;

import com.netease.pangu.game.distribution.Slave;
import com.netease.pangu.game.distribution.SlaveManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SlaveScheduleService {
    @Resource
    private SlaveManager slaveManager;

    public Slave getSlaveByScheduled() {
        return DemoScheduler.getInstance().schedule(slaveManager);
    }
}
