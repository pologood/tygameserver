package com.netease.pangu.game.common.meta;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class GameContext<T extends IPlayer> {
	private final ChannelHandlerContext channelHandlerContext;
	private final PlayerSession<T> playerSession;
	private final String rpcMethodName;
	private final Object frame;
	public GameContext(ChannelHandlerContext context, PlayerSession<T> playerSession, String rpcMethodName, Object frame){
		this.channelHandlerContext = context;
		this.playerSession = playerSession;
		this.rpcMethodName = rpcMethodName;
		this.frame = frame;
		
	}
	public ChannelHandlerContext getChannelHandlerContext() {
		return channelHandlerContext;
	}
	
	public Channel getChannel() {
		return channelHandlerContext.channel();
	}
	
	public PlayerSession<T> getPlayerSession() {
		return playerSession;
	}
	
	public Object getFrame() {
		return frame;
	}
	
	public String getRpcMethodName() {
		return rpcMethodName;
	}
}
