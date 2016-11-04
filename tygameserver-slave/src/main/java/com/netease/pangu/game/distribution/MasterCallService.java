package com.netease.pangu.game.distribution;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.netease.pangu.distribution.proto.AppNode;
import com.netease.pangu.distribution.proto.MasterServiceGrpc;
import com.netease.pangu.distribution.proto.MasterServiceGrpc.MasterServiceBlockingStub;
import com.netease.pangu.distribution.proto.RpcResponse;
import com.netease.pangu.game.service.GameRoomManager;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

@Service
public class MasterCallService {
	@Resource
	private GameRoomManager gameRoomManager;
	private MasterServiceBlockingStub stub;
	private AtomicBoolean isInit = new AtomicBoolean(false);
	public void init(String ip, int port) {
		ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext(true).build();
		stub = MasterServiceGrpc.newBlockingStub(channel);
		isInit.set(true);
	}
	
	public boolean isInit(){
		return isInit.get();
	}
	
	public RpcResponse addOrUpdateNode(Node worker) {
		AppNode.Builder request = AppNode.newBuilder();
		request.setName(worker.getName());
		request.setIp(worker.getIp());
		RpcResponse response = null;
		try {
			request.setPort(worker.getPort());
			response = stub.addOrUpdateNode(request.build());
		} catch (StatusRuntimeException e) {
			e.printStackTrace();
		}
		return response;
	}

}
