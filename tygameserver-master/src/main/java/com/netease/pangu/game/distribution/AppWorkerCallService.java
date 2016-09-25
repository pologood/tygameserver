package com.netease.pangu.game.distribution;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;
import com.netease.pangu.distribution.proto.AppWorkerServiceGrpc;
import com.netease.pangu.distribution.proto.MethodRequest;
import com.netease.pangu.distribution.proto.RpcResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Service
public class AppWorkerCallService {
	private ConcurrentMap<String, AppWorkerServiceGrpc.AppWorkerServiceFutureStub> futureStubMap = new ConcurrentHashMap();
	private ConcurrentMap<String, AppWorkerServiceGrpc.AppWorkerServiceBlockingStub> blockingStubMap = new ConcurrentHashMap();
	
	public boolean addBlockingStub(AppWorker worker){
		ManagedChannel channel = ManagedChannelBuilder.forAddress(worker.getIp(), worker.getPort()).usePlaintext(true).build();
		AppWorkerServiceGrpc.AppWorkerServiceBlockingStub stub = AppWorkerServiceGrpc.newBlockingStub(channel);
		return blockingStubMap.putIfAbsent(AppWorkerManager.getKey(worker), stub) == null;
	}
	
	public boolean addFutureStub(AppWorker worker){
		ManagedChannel channel = ManagedChannelBuilder.forAddress(worker.getIp(), worker.getPort()).usePlaintext(true).build();
		AppWorkerServiceGrpc.AppWorkerServiceFutureStub stub = AppWorkerServiceGrpc.newFutureStub(channel);
		return futureStubMap.putIfAbsent(AppWorkerManager.getKey(worker), stub) == null;
	}
	
	public void callFuture(AppWorker worker, String beanName, String methodName, List<Any> args){
		AppWorkerServiceGrpc.AppWorkerServiceFutureStub stub = futureStubMap.get(AppWorkerManager.getKey(worker));
		MethodRequest.Builder mBuilder = MethodRequest.newBuilder();
		mBuilder.setBeanName(beanName);
		mBuilder.setMethodName(methodName);
		mBuilder.addAllArgs(args);
		MethodRequest request = mBuilder.build();
		ListenableFuture<RpcResponse> lRespones = stub.call(request);
		Futures.addCallback(lRespones,  new FutureCallback<RpcResponse>() {

			@Override
			public void onSuccess(RpcResponse result) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFailure(Throwable t) {
				// TODO Auto-generated method stub
				
			}
		} );
	}
	
}
