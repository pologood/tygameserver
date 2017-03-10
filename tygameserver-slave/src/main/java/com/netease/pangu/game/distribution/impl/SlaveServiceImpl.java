package com.netease.pangu.game.distribution.impl;

import com.netease.pangu.distribution.proto.MethodRequest;
import com.netease.pangu.distribution.proto.RpcResponse;
import com.netease.pangu.distribution.proto.SlaveServiceGrpc;
import com.netease.pangu.game.util.ReturnUtils;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class SlaveServiceImpl extends SlaveServiceGrpc.SlaveServiceImplBase {
    private final static Logger logger = Logger.getLogger(SlaveServiceImpl.class);

    @Override
    public void call(MethodRequest request, StreamObserver<RpcResponse> responseObserver) {
        logger.info(request.getBeanName());
        RpcResponse.Builder builder = RpcResponse.newBuilder();
        builder.setCode(ReturnUtils.SUCC);

        builder.setMessage("call method not implemented");
        RpcResponse result = builder.build();
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }
}
