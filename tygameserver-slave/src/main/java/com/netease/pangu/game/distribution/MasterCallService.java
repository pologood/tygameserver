package com.netease.pangu.game.distribution;

import com.netease.pangu.distribution.proto.MasterServiceGrpc;
import com.netease.pangu.distribution.proto.MasterServiceGrpc.MasterServiceBlockingStub;
import com.netease.pangu.distribution.proto.RpcResponse;
import com.netease.pangu.game.service.RoomService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MasterCallService {
    @Resource
    private RoomService gameRoomManager;
    private MasterServiceBlockingStub stub;
    private AtomicBoolean isInit = new AtomicBoolean(false);

    public void init(String ip, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext(true).build();
        stub = MasterServiceGrpc.newBlockingStub(channel);
        isInit.set(true);
    }

    public boolean isInit() {
        return isInit.get();
    }

    public RpcResponse addOrUpdateSlave(Slave worker) {
        com.netease.pangu.distribution.proto.Slave.Builder request = com.netease.pangu.distribution.proto.Slave.newBuilder();
        request.setName(worker.getName());
        request.setIp(worker.getIp());
        request.setPort(worker.getPort());
        request.setCount(worker.getCount());
        RpcResponse response = null;
        try {
            response = stub.addOrUpdateSlave(request.build());
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
        return response;
    }

}
