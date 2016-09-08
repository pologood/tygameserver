// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: distribute.proto

package com.netease.pangu.distribution.proto;

/**
 * Protobuf service {@code distribution.AppWorkerService}
 */
public  abstract class AppWorkerService
    implements com.google.protobuf.Service {
  protected AppWorkerService() {}

  public interface Interface {
    /**
     * <code>rpc call(.distribution.MethodRequest) returns (.distribution.RpcResponse);</code>
     */
    public abstract void call(
        com.google.protobuf.RpcController controller,
        com.netease.pangu.distribution.proto.MethodRequest request,
        com.google.protobuf.RpcCallback<com.netease.pangu.distribution.proto.RpcResponse> done);

  }

  public static com.google.protobuf.Service newReflectiveService(
      final Interface impl) {
    return new AppWorkerService() {
      @java.lang.Override
      public  void call(
          com.google.protobuf.RpcController controller,
          com.netease.pangu.distribution.proto.MethodRequest request,
          com.google.protobuf.RpcCallback<com.netease.pangu.distribution.proto.RpcResponse> done) {
        impl.call(controller, request, done);
      }

    };
  }

  public static com.google.protobuf.BlockingService
      newReflectiveBlockingService(final BlockingInterface impl) {
    return new com.google.protobuf.BlockingService() {
      public final com.google.protobuf.Descriptors.ServiceDescriptor
          getDescriptorForType() {
        return getDescriptor();
      }

      public final com.google.protobuf.Message callBlockingMethod(
          com.google.protobuf.Descriptors.MethodDescriptor method,
          com.google.protobuf.RpcController controller,
          com.google.protobuf.Message request)
          throws com.google.protobuf.ServiceException {
        if (method.getService() != getDescriptor()) {
          throw new java.lang.IllegalArgumentException(
            "Service.callBlockingMethod() given method descriptor for " +
            "wrong service type.");
        }
        switch(method.getIndex()) {
          case 0:
            return impl.call(controller, (com.netease.pangu.distribution.proto.MethodRequest)request);
          default:
            throw new java.lang.AssertionError("Can't get here.");
        }
      }

      public final com.google.protobuf.Message
          getRequestPrototype(
          com.google.protobuf.Descriptors.MethodDescriptor method) {
        if (method.getService() != getDescriptor()) {
          throw new java.lang.IllegalArgumentException(
            "Service.getRequestPrototype() given method " +
            "descriptor for wrong service type.");
        }
        switch(method.getIndex()) {
          case 0:
            return com.netease.pangu.distribution.proto.MethodRequest.getDefaultInstance();
          default:
            throw new java.lang.AssertionError("Can't get here.");
        }
      }

      public final com.google.protobuf.Message
          getResponsePrototype(
          com.google.protobuf.Descriptors.MethodDescriptor method) {
        if (method.getService() != getDescriptor()) {
          throw new java.lang.IllegalArgumentException(
            "Service.getResponsePrototype() given method " +
            "descriptor for wrong service type.");
        }
        switch(method.getIndex()) {
          case 0:
            return com.netease.pangu.distribution.proto.RpcResponse.getDefaultInstance();
          default:
            throw new java.lang.AssertionError("Can't get here.");
        }
      }

    };
  }

  /**
   * <code>rpc call(.distribution.MethodRequest) returns (.distribution.RpcResponse);</code>
   */
  public abstract void call(
      com.google.protobuf.RpcController controller,
      com.netease.pangu.distribution.proto.MethodRequest request,
      com.google.protobuf.RpcCallback<com.netease.pangu.distribution.proto.RpcResponse> done);

  public static final
      com.google.protobuf.Descriptors.ServiceDescriptor
      getDescriptor() {
    return com.netease.pangu.distribution.proto.GameDistributedProto.getDescriptor().getServices().get(1);
  }
  public final com.google.protobuf.Descriptors.ServiceDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }

  public final void callMethod(
      com.google.protobuf.Descriptors.MethodDescriptor method,
      com.google.protobuf.RpcController controller,
      com.google.protobuf.Message request,
      com.google.protobuf.RpcCallback<
        com.google.protobuf.Message> done) {
    if (method.getService() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "Service.callMethod() given method descriptor for wrong " +
        "service type.");
    }
    switch(method.getIndex()) {
      case 0:
        this.call(controller, (com.netease.pangu.distribution.proto.MethodRequest)request,
          com.google.protobuf.RpcUtil.<com.netease.pangu.distribution.proto.RpcResponse>specializeCallback(
            done));
        return;
      default:
        throw new java.lang.AssertionError("Can't get here.");
    }
  }

  public final com.google.protobuf.Message
      getRequestPrototype(
      com.google.protobuf.Descriptors.MethodDescriptor method) {
    if (method.getService() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "Service.getRequestPrototype() given method " +
        "descriptor for wrong service type.");
    }
    switch(method.getIndex()) {
      case 0:
        return com.netease.pangu.distribution.proto.MethodRequest.getDefaultInstance();
      default:
        throw new java.lang.AssertionError("Can't get here.");
    }
  }

  public final com.google.protobuf.Message
      getResponsePrototype(
      com.google.protobuf.Descriptors.MethodDescriptor method) {
    if (method.getService() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "Service.getResponsePrototype() given method " +
        "descriptor for wrong service type.");
    }
    switch(method.getIndex()) {
      case 0:
        return com.netease.pangu.distribution.proto.RpcResponse.getDefaultInstance();
      default:
        throw new java.lang.AssertionError("Can't get here.");
    }
  }

  public static Stub newStub(
      com.google.protobuf.RpcChannel channel) {
    return new Stub(channel);
  }

  public static final class Stub extends com.netease.pangu.distribution.proto.AppWorkerService implements Interface {
    private Stub(com.google.protobuf.RpcChannel channel) {
      this.channel = channel;
    }

    private final com.google.protobuf.RpcChannel channel;

    public com.google.protobuf.RpcChannel getChannel() {
      return channel;
    }

    public  void call(
        com.google.protobuf.RpcController controller,
        com.netease.pangu.distribution.proto.MethodRequest request,
        com.google.protobuf.RpcCallback<com.netease.pangu.distribution.proto.RpcResponse> done) {
      channel.callMethod(
        getDescriptor().getMethods().get(0),
        controller,
        request,
        com.netease.pangu.distribution.proto.RpcResponse.getDefaultInstance(),
        com.google.protobuf.RpcUtil.generalizeCallback(
          done,
          com.netease.pangu.distribution.proto.RpcResponse.class,
          com.netease.pangu.distribution.proto.RpcResponse.getDefaultInstance()));
    }
  }

  public static BlockingInterface newBlockingStub(
      com.google.protobuf.BlockingRpcChannel channel) {
    return new BlockingStub(channel);
  }

  public interface BlockingInterface {
    public com.netease.pangu.distribution.proto.RpcResponse call(
        com.google.protobuf.RpcController controller,
        com.netease.pangu.distribution.proto.MethodRequest request)
        throws com.google.protobuf.ServiceException;
  }

  private static final class BlockingStub implements BlockingInterface {
    private BlockingStub(com.google.protobuf.BlockingRpcChannel channel) {
      this.channel = channel;
    }

    private final com.google.protobuf.BlockingRpcChannel channel;

    public com.netease.pangu.distribution.proto.RpcResponse call(
        com.google.protobuf.RpcController controller,
        com.netease.pangu.distribution.proto.MethodRequest request)
        throws com.google.protobuf.ServiceException {
      return (com.netease.pangu.distribution.proto.RpcResponse) channel.callBlockingMethod(
        getDescriptor().getMethods().get(0),
        controller,
        request,
        com.netease.pangu.distribution.proto.RpcResponse.getDefaultInstance());
    }

  }

  // @@protoc_insertion_point(class_scope:distribution.AppWorkerService)
}

