package com.netease.pangu.game.distribution.impl;

import com.netease.pangu.distribution.proto.MasterServiceGrpc;
import com.netease.pangu.distribution.proto.RpcResponse;
import com.netease.pangu.game.distribution.Slave;
import com.netease.pangu.game.distribution.SlaveManager;
import com.netease.pangu.game.util.JsonUtil;
import com.netease.pangu.game.util.ReturnUtils;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MasterServiceImpl extends MasterServiceGrpc.MasterServiceImplBase {
    private final static Logger logger = Logger.getLogger(MasterServiceImpl.class);

    @Resource
    private SlaveManager slaveManager;

    @Override
    public void addOrUpdateSlave(com.netease.pangu.distribution.proto.Slave request, StreamObserver<RpcResponse> responseObserver) {
        Slave node = new Slave();
        node.setIp(request.getIp());
        node.setName(request.getName());
        node.setPort(request.getPort());
        node.setSys(request.getSysMap());
        node.setCount(request.getCount());
        RpcResponse.Builder builder = RpcResponse.newBuilder();

        if (!slaveManager.contains(node)) {
            if (slaveManager.add(node)) {
                builder.setCode(ReturnUtils.SUCC);
                builder.setMessage(JsonUtil.toJson(slaveManager.getWorkersMap()));
            } else {
                builder.setCode(ReturnUtils.FAILED);
                builder.setMessage("add failed");
            }
        } else {
            if (slaveManager.update(node)) {
                builder.setCode(ReturnUtils.SUCC);
                builder.setMessage(JsonUtil.toJson(slaveManager.getWorkersMap()));
            } else {
                builder.setCode(ReturnUtils.FAILED);
                builder.setMessage("add failed");
            }
        }

        RpcResponse result = builder.build();
        responseObserver.onNext(result);
        responseObserver.onCompleted();

    }
}
