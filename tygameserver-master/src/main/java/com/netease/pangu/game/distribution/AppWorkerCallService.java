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
	private ConcurrentMap<String, AppWorkerServiceGrpc.AppWorkerServiceFutureStub> futureStubMap = new ConcurrentHashMap<String, AppWorkerServiceGrpc.AppWorkerServiceFutureStub>();
	private ConcurrentMap<String, AppWorkerServiceGrpc.AppWorkerServiceBlockingStub> blockingStubMap = new ConcurrentHashMap<String, AppWorkerServiceGrpc.AppWorkerServiceBlockingStub>();
	
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
	
	public void call(AppWorker worker, String beanName, String methodName, List<Any> args){
		AppWorkerServiceGrpc.AppWorkerServiceBlockingStub stub = blockingStubMap.get(AppWorkerManager.getKey(worker));
		MethodRequest.Builder mBuilder = MethodRequest.newBuilder();
		mBuilder.setBeanName(beanName);
		mBuilder.setMethodName(methodName);
		mBuilder.addAllArgs(args);
		MethodRequest request = mBuilder.build();
		RpcResponse response = stub.call(request);
		System.out.println(response.getCode());
	}
	
	public void callFuture(AppWorker worker, String beanName, String methodName, List<Any> args){
		AppWorkerServiceGrpc.AppWorkerServiceFutureStub stub = futureStubMap.get(AppWorkerManager.getKey(worker));
		MethodRequest.Builder mBuilder = MethodRequest.newBuilder();
		mBuilder.setBeanName(beanName);
		mBuilder.setMethodName(methodName);
		mBuilder.addAllArgs(args);
		MethodRequest request = mBuilder.build();
		ListenableFuture<RpcResponse> lResponse = stub.call(request);
		
		Futures.addCallback(lResponse, new FutureCallback<RpcResponse>() {

			@Override
			public void onSuccess(RpcResponse result) {
				System.out.println(result.getCode());
			}

			@Override
			public void onFailure(Throwable t) {
			}
		});
	}
	
}