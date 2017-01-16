package com.netease.pangu.game.common.meta;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class GameContext<T> {
    private final ChannelHandlerContext channelHandlerContext;
    private final T session;
    private final String rpcMethodName;
    private final Object frame;

    public GameContext(ChannelHandlerContext context, T session, String rpcMethodName, Object frame) {
        this.channelHandlerContext = context;
        this.session = session;
        this.rpcMethodName = rpcMethodName;
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

    public String getRpcMethodName() {
        return rpcMethodName;
    }

    public T getSession() {
        return session;
    }
}
