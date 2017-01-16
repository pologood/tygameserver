package com.netease.pangu.game.distribution;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;
import com.netease.pangu.distribution.proto.MethodRequest;
import com.netease.pangu.distribution.proto.NodeServiceGrpc;
import com.netease.pangu.distribution.proto.RpcResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class NodeCallService {
    private ConcurrentMap<String, NodeServiceGrpc.NodeServiceFutureStub> futureStubMap = new ConcurrentHashMap<String, NodeServiceGrpc.NodeServiceFutureStub>();
    private ConcurrentMap<String, NodeServiceGrpc.NodeServiceBlockingStub> blockingStubMap = new ConcurrentHashMap<String, NodeServiceGrpc.NodeServiceBlockingStub>();

    public boolean addBlockingStub(Node worker) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(worker.getIp(), worker.getPort()).usePlaintext(true).build();
        NodeServiceGrpc.NodeServiceBlockingStub stub = NodeServiceGrpc.newBlockingStub(channel);
        return blockingStubMap.putIfAbsent(worker.getName(), stub) == null;
    }

    public boolean addFutureStub(Node worker) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(worker.getIp(), worker.getPort()).usePlaintext(true).build();
        NodeServiceGrpc.NodeServiceFutureStub stub = NodeServiceGrpc.newFutureStub(channel);
        return futureStubMap.putIfAbsent(worker.getName(), stub) == null;
    }

    public void callFuture(Node worker, String beanName, String methodName, List<Any> args) {
        NodeServiceGrpc.NodeServiceFutureStub stub = futureStubMap.get(worker.getName());
        MethodRequest.Builder mBuilder = MethodRequest.newBuilder();
        mBuilder.setBeanName(beanName);
        mBuilder.setMethodName(methodName);
        mBuilder.addAllArgs(args);
        MethodRequest request = mBuilder.build();
        ListenableFuture<RpcResponse> lRespones = stub.call(request);
        Futures.addCallback(lRespones, new FutureCallback<RpcResponse>() {

            @Override
            public void onSuccess(RpcResponse result) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFailure(Throwable t) {
                // TODO Auto-generated method stub

            }
        });
    }

}
