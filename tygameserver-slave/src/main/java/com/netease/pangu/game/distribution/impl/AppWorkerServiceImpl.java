package com.netease.pangu.game.distribution.impl;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.netease.pangu.distribution.proto.AppWorkerServiceGrpc;
import com.netease.pangu.distribution.proto.MethodRequest;
import com.netease.pangu.distribution.proto.RpcResponse;
import com.netease.pangu.game.util.ReturnUtils;

import io.grpc.stub.StreamObserver;

@Component
public class AppWorkerServiceImpl extends AppWorkerServiceGrpc.AppWorkerServiceImplBase {
	private final static Logger logger = Logger.getLogger(AppWorkerServiceImpl.class);
	@Override
	public void call(MethodRequest request, StreamObserver<RpcResponse> responseObserver) {
		logger.info(request.getBeanName());
		RpcResponse.Builder builder = RpcResponse.newBuilder();
		builder.setCode(ReturnUtils.SUCC);
		builder.setMessage("hello");
		RpcResponse result = builder.build();
		responseObserver.onNext(result);
		responseObserver.onCompleted();
	}
}
