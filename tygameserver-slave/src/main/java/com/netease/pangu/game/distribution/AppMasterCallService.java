package com.netease.pangu.game.distribution;

import org.springframework.stereotype.Service;

import com.netease.pangu.distribution.proto.AppMasterServiceGrpc;
import com.netease.pangu.distribution.proto.AppMasterServiceGrpc.AppMasterServiceBlockingStub;
import com.netease.pangu.distribution.proto.RpcResponse;
import com.netease.pangu.distribution.proto.Worker;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

@Service
public class AppMasterCallService {
	private AppMasterServiceBlockingStub stub;

	public void init(String ip, int port) {
		ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext(true).build();
		stub = AppMasterServiceGrpc.newBlockingStub(channel);
	}

	public void addWorker(AppWorker worker) {
		Worker.Builder request = Worker.newBuilder();
		request.setName(worker.getName());
		request.setIp(worker.getIp());
		RpcResponse response;
		try {
			request.setPort(worker.getPort());
			response = stub.addAppWorker(request.build());
		} catch (StatusRuntimeException e) {
			System.out.println(e.getMessage());
		}
	}

}
