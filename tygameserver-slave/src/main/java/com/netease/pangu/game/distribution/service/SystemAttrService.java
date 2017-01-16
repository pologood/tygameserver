package com.netease.pangu.game.distribution.service;

import com.netease.pangu.game.distribution.Node;
import org.springframework.stereotype.Service;

@Service
public class SystemAttrService {
    private Node node;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }


}
