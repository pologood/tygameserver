package com.netease.pangu.game.distribution.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.netease.pangu.distribution.proto.AppMasterServiceGrpc;
import com.netease.pangu.distribution.proto.RpcResponse;
import com.netease.pangu.distribution.proto.Worker;
import com.netease.pangu.game.distribution.AppWorker;
import com.netease.pangu.game.distribution.AppWorkerManager;
import com.netease.pangu.game.util.JsonUtil;
import com.netease.pangu.game.util.ReturnUtils;

import io.grpc.stub.StreamObserver;

@Component
public class AppMasterServiceImpl extends AppMasterServiceGrpc.AppMasterServiceImplBase {
	private @Resource AppWorkerManager appWorkerManager;
	@Override
	public void addAppWorker(Worker request, StreamObserver<RpcResponse> responseObserver) {
		AppWorker appWorker = new AppWorker();
		appWorker.setIp(request.getIp());
		appWorker.setName(request.getName());
		appWorker.setPort(request.getPort());
		appWorker.setSys(request.getSysMap());
		appWorker.setCount(request.getCount());
		RpcResponse.Builder builder = RpcResponse.newBuilder();
		if(appWorkerManager.addNode(appWorker)){
			builder.setCode(ReturnUtils.SUCC);
			builder.setMessage(JsonUtil.toJson(appWorkerManager.getWorkersMap()));
		}else{
			builder.setCode(ReturnUtils.FAILED);
			builder.setMessage("add failed");
		}
		
		RpcResponse result = builder.build();
		responseObserver.onNext(result);
		responseObserver.onCompleted();
	}
}
