package com.netease.pangu.game.distribution;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.netease.pangu.distribution.proto.AppMasterServiceGrpc;
import com.netease.pangu.distribution.proto.AppMasterServiceGrpc.AppMasterServiceBlockingStub;
import com.netease.pangu.game.service.GameRoomManager;
import com.netease.pangu.distribution.proto.RpcResponse;
import com.netease.pangu.distribution.proto.Worker;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

@Service
public class AppMasterCallService {
	@Resource
	private GameRoomManager gameRoomManager;
	private AppMasterServiceBlockingStub stub;
	private AtomicBoolean isInit = new AtomicBoolean(false);
	public void init(String ip, int port) {
		ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext(true).build();
		stub = AppMasterServiceGrpc.newBlockingStub(channel);
		isInit.set(true);
	}
	
	public boolean isInit(){
		return isInit.get();
	}
	
	public RpcResponse addOrUpdateWorker(AppWorker worker) {
		Worker.Builder request = Worker.newBuilder();
		request.setName(worker.getName());
		request.setIp(worker.getIp());
		RpcResponse response = null;
		try {
			request.setPort(worker.getPort());
			response = stub.addOrUpdateAppWorker(request.build());
		} catch (StatusRuntimeException e) {
			e.printStackTrace();
		}
		return response;
	}

}
