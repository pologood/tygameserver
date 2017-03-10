package com.netease.pangu.game.distribution;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;
import com.netease.pangu.distribution.proto.MethodRequest;
import com.netease.pangu.distribution.proto.RpcResponse;
import com.netease.pangu.distribution.proto.SlaveServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class SlaveCallService {
    private ConcurrentMap<String, SlaveServiceGrpc.SlaveServiceFutureStub> futureStubMap = new ConcurrentHashMap<String, SlaveServiceGrpc.SlaveServiceFutureStub>();
    private ConcurrentMap<String, SlaveServiceGrpc.SlaveServiceBlockingStub> blockingStubMap = new ConcurrentHashMap<String, SlaveServiceGrpc.SlaveServiceBlockingStub>();

    public boolean addBlockingStub(Slave worker) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(worker.getIp(), worker.getPort()).usePlaintext(true).build();
        SlaveServiceGrpc.SlaveServiceBlockingStub stub = SlaveServiceGrpc.newBlockingStub(channel);
        return blockingStubMap.putIfAbsent(worker.getName(), stub) == null;
    }

    public boolean addFutureStub(Slave worker) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(worker.getIp(), worker.getPort()).usePlaintext(true).build();
        SlaveServiceGrpc.SlaveServiceFutureStub stub = SlaveServiceGrpc.newFutureStub(channel);
        return futureStubMap.putIfAbsent(worker.getName(), stub) == null;
    }

    public void callFuture(Slave worker, String beanName, String methodName, List<Any> args) {
        SlaveServiceGrpc.SlaveServiceFutureStub stub = futureStubMap.get(worker.getName());
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
