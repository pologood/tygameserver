package com.netease.pangu.game.distribution.impl;

import org.springframework.stereotype.Service;

import com.netease.pangu.distribution.proto.AppMasterServiceGrpc.AppMasterServiceBlockingStub;

@Service
public class AppMasterCallService {
	private AppMasterServiceBlockingStub stub;
	
}
