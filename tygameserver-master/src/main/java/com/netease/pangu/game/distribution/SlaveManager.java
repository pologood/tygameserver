package com.netease.pangu.game.distribution;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SlaveManager {
    private ConcurrentMap<String, Slave> slaves = new ConcurrentHashMap<>();

    public Map<String, Slave> getWorkersMap() {
        return Collections.unmodifiableMap(slaves);
    }

    public Slave get(String name) {
        return slaves.get(name);
    }

    public boolean contains(Slave worker) {
        return slaves.containsKey(worker.getName());
    }

    public boolean add(Slave worker) {
        return slaves.putIfAbsent(worker.getName(), worker) == null;
    }

    public boolean update(Slave worker) {
        return slaves.replace(worker.getName(), worker) != null;
    }

    public boolean remove(Slave worker) {
        return slaves.remove(worker.getName()) != null;
    }
}
