package com.netease.pangu.distribution.proto;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.0.0)",
    comments = "Source: distribute.proto")
public class AppWorkerServiceGrpc {

  private AppWorkerServiceGrpc() {}

  public static final String SERVICE_NAME = "distribution.AppWorkerService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.netease.pangu.distribution.proto.MethodRequest,
      com.netease.pangu.distribution.proto.RpcResponse> METHOD_CALL =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "distribution.AppWorkerService", "call"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.netease.pangu.distribution.proto.MethodRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.netease.pangu.distribution.proto.RpcResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static AppWorkerServiceStub newStub(io.grpc.Channel channel) {
    return new AppWorkerServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static AppWorkerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new AppWorkerServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static AppWorkerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new AppWorkerServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class AppWorkerServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void call(com.netease.pangu.distribution.proto.MethodRequest request,
        io.grpc.stub.StreamObserver<com.netease.pangu.distribution.proto.RpcResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CALL, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_CALL,
            asyncUnaryCall(
              new MethodHandlers<
                com.netease.pangu.distribution.proto.MethodRequest,
                com.netease.pangu.distribution.proto.RpcResponse>(
                  this, METHODID_CALL)))
          .build();
    }
  }

  /**
   */
  public static final class AppWorkerServiceStub extends io.grpc.stub.AbstractStub<AppWorkerServiceStub> {
    private AppWorkerServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AppWorkerServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AppWorkerServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AppWorkerServiceStub(channel, callOptions);
    }

    /**
     */
    public void call(com.netease.pangu.distribution.proto.MethodRequest request,
        io.grpc.stub.StreamObserver<com.netease.pangu.distribution.proto.RpcResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CALL, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class AppWorkerServiceBlockingStub extends io.grpc.stub.AbstractStub<AppWorkerServiceBlockingStub> {
    private AppWorkerServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AppWorkerServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AppWorkerServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AppWorkerServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.netease.pangu.distribution.proto.RpcResponse call(com.netease.pangu.distribution.proto.MethodRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CALL, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class AppWorkerServiceFutureStub extends io.grpc.stub.AbstractStub<AppWorkerServiceFutureStub> {
    private AppWorkerServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AppWorkerServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AppWorkerServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AppWorkerServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.netease.pangu.distribution.proto.RpcResponse> call(
        com.netease.pangu.distribution.proto.MethodRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CALL, getCallOptions()), request);
    }
  }

  private static final int METHODID_CALL = 0;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AppWorkerServiceImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(AppWorkerServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CALL:
          serviceImpl.call((com.netease.pangu.distribution.proto.MethodRequest) request,
              (io.grpc.stub.StreamObserver<com.netease.pangu.distribution.proto.RpcResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    return new io.grpc.ServiceDescriptor(SERVICE_NAME,
        METHOD_CALL);
  }

}
