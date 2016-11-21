package com.netease.pangu.game.distribution.impl;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.netease.pangu.distribution.proto.AppNode;
import com.netease.pangu.distribution.proto.MasterServiceGrpc;
import com.netease.pangu.distribution.proto.RpcResponse;
import com.netease.pangu.game.distribution.Node;
import com.netease.pangu.game.distribution.NodeManager;
import com.netease.pangu.game.util.JsonUtil;
import com.netease.pangu.game.util.ReturnUtils;

import io.grpc.stub.StreamObserver;

@Component
public class MasterServiceImpl extends MasterServiceGrpc.MasterServiceImplBase {
	private final static Logger logger = Logger.getLogger(MasterServiceImpl.class);
	private @Resource NodeManager nodeManager;
	@Override
	public void addOrUpdateNode(AppNode request, StreamObserver<RpcResponse> responseObserver) {
		Node node = new Node();
		node.setIp(request.getIp());
		node.setName(request.getName());
		node.setPort(request.getPort());
		node.setSys(request.getSysMap());
		node.setCount(request.getCount());
		RpcResponse.Builder builder = RpcResponse.newBuilder();
		
		if(!nodeManager.contains(node)){
			if(nodeManager.addNode(node)){
				builder.setCode(ReturnUtils.SUCC);
				builder.setMessage(JsonUtil.toJson(nodeManager.getWorkersMap()));
			}else{
				builder.setCode(ReturnUtils.FAILED);
				builder.setMessage("add failed");
			}
		}else{
			if(nodeManager.updateNode(node)){
				builder.setCode(ReturnUtils.SUCC);
				builder.setMessage(JsonUtil.toJson(nodeManager.getWorkersMap()));
			}else{
				builder.setCode(ReturnUtils.FAILED);
				builder.setMessage("add failed");
			}
		}
		RpcResponse result = builder.build();
		logger.info(JsonUtil.toJson(result));
		responseObserver.onNext(result);
		responseObserver.onCompleted();
	
	}
}
