package com.netease.pangu.game.common.meta;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class GameContext {
	private final ChannelHandlerContext channelHandlerContext;
	private final PlayerSession playerSession;
	private final Object frame;
	public GameContext(ChannelHandlerContext context, PlayerSession playerSession, Object frame){
		this.channelHandlerContext = context;
		this.playerSession = playerSession;
		this.frame = frame;
		
	}
	public ChannelHandlerContext getChannelHandlerContext() {
		return channelHandlerContext;
	}
	
	public Channel getChannel() {
		return channelHandlerContext.channel();
	}
	
	public PlayerSession getPlayerSession() {
		return playerSession;
	}
	
	public Object getFrame() {
		return frame;
	}
}
