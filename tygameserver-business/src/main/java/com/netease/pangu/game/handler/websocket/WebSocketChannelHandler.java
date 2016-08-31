package com.netease.pangu.game.handler.websocket;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.common.meta.PlayerSession;
import com.netease.pangu.game.rpc.NettyRpcCallInvoker;
import com.netease.pangu.game.service.PlayerManager;
import com.netease.pangu.game.service.PlayerSessionManager;
import com.netease.pangu.game.util.JsonUtil;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

@Component
@Lazy
@Sharable
public class WebSocketChannelHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	@Resource
	private NettyRpcCallInvoker nettyRpcCallInvoker;
	@Resource
	private PlayerSessionManager playerSessionManager;
	@Resource
	private PlayerManager playerManager;
	
	private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
            TextWebSocketFrame frame) throws Exception { 
        String dataStr = ((TextWebSocketFrame) frame).text();
		Map<String, Object> data = JsonUtil.fromJson(dataStr);
		String rpcMethodName = (String)data.get("rpcMethod");
		String sessionId = (String)data.get("sessionId");
		@SuppressWarnings("unchecked")
		List<Object> args = (List<Object>)data.get("params");
		GameContext context = null;
		if(Strings.isNullOrEmpty(sessionId)){
			context = new GameContext(ctx, null, frame);	
		}else{
			Double num = NumberUtils.toDouble(sessionId);
			long playerSessionId = num.longValue();
			PlayerSession playerSession = playerSessionManager.getSession(playerSessionId);
			if(playerSession != null){			
				context = new GameContext(ctx, playerSession, frame);
			}else{			
				GameResult result = ReturnUtils.failed(rpcMethodName, "user hasn't registered");
				ctx.channel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(result)));
				return;
			}
		}
		nettyRpcCallInvoker.invoke(rpcMethodName, args, context);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception { 
        Channel incoming = ctx.channel();
        // Broadcast a message to multiple Channels
        System.out.println("[SERVER] - " + incoming.remoteAddress() + " active");
        channels.add(incoming);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
        	Channel incoming = ctx.channel();
        	System.out.println("[SERVER] - " + incoming.remoteAddress() + " leave");
        }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
        Channel incoming = ctx.channel();
        System.out.println("[SERVER] - " + incoming.remoteAddress() + " active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
        System.out.println("[SERVER] - " + incoming.remoteAddress() + " inactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        Channel incoming = ctx.channel();
        cause.printStackTrace();
        ctx.close();
    }
}