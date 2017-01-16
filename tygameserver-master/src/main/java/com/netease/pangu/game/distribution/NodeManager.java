package com.netease.pangu.game.distribution;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class NodeManager {
    private ConcurrentMap<String, Node> nodes = new ConcurrentHashMap<>();

    public Map<String, Node> getWorkersMap() {
        return Collections.unmodifiableMap(nodes);
    }

    public Node getNode(String name) {
        return nodes.get(name);
    }

    public boolean contains(Node worker) {
        return nodes.containsKey(worker.getName());
    }

    public boolean addNode(Node worker) {
        return nodes.putIfAbsent(worker.getName(), worker) == null;
    }

    public boolean updateNode(Node worker) {
        return nodes.replace(worker.getName(), worker) != null;
    }

    public boolean removeNode(Node worker) {
        return nodes.remove(worker.getName()) != null;
    }
}
