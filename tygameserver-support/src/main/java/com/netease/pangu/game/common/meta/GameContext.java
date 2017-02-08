package com.netease.pangu.game.common.meta;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class GameContext<T> {
    private final ChannelHandlerContext channelHandlerContext;
    private final T session;
    private final String rpcMethod;
    private final Object frame;

    public GameContext(ChannelHandlerContext context, T session, String rpcMethod, Object frame) {
        this.channelHandlerContext = context;
        this.session = session;
        this.rpcMethod = rpcMethod;
        this.frame = frame;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public Channel getChannel() {
        return channelHandlerContext.channel();
    }

    public Object getFrame() {
        return frame;
    }

    public String getRpcMethod() {
        return rpcMethod;
    }

    public T getSession() {
        return session;
    }
}
