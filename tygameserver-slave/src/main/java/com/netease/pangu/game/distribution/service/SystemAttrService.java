package com.netease.pangu.game.distribution.service;

import org.springframework.stereotype.Service;

import com.netease.pangu.game.distribution.Node;

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
