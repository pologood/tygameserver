package com.netease.pangu.game.distribution.service;

import com.netease.pangu.game.distribution.Slave;
import org.springframework.stereotype.Service;

@Service
public class SystemAttrService {
    private Slave slave;

    public Slave getSlave() {
        return slave;
    }

    public void setSlave(Slave slave) {
        this.slave = slave;
    }


}
