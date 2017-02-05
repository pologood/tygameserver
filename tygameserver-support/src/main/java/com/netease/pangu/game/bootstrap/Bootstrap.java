package com.netease.pangu.game.bootstrap;

import org.springframework.context.ConfigurableApplicationContext;

public interface Bootstrap {
    void init(ConfigurableApplicationContext context);

    void start();

    void stop();
}
