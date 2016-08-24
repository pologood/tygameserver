package com.netease.pangu.game.handler.websocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.netease.pangu.game.controller.BusinessCode;
import com.netease.pangu.game.controller.BusinessCode.GameResult;
import com.netease.pangu.game.meta.Player;
import com.netease.pangu.game.meta.PlayerSession;
import com.netease.pangu.game.service.NettyRpcCallInvoker;
import com.netease.pangu.game.service.PlayerManager;
import com.netease.pangu.game.service.PlayerSessionManager;
import com.netease.pangu.game.util.JsonUtil;

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
	
	public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
            TextWebSocketFrame frame) throws Exception { 
        String dataStr = ((TextWebSocketFrame) frame).text();
		Map<String, Object> data = JsonUtil.fromJson(dataStr);
		String rpcMethodName = (String)data.get("rpcMethod");
		String sessionId = (String)data.get("sessionId");
		@SuppressWarnings("unchecked")
		List<Object> args = (List<Object>)data.get("params");
		if(StringUtils.equals(rpcMethodName.toLowerCase(), "register")){
			String name =(String)args.get(0);
			Player player = playerManager.createPlayer(name);
			PlayerSession playerSession = playerSessionManager.createPlayerSession(player.getId(), ctx.channel());
			GameResult result = new GameResult();
			result.setCode(BusinessCode.SUCC);
			result.setRpcMethodName(rpcMethodName);
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("sessionId", playerSession.getPlayerId());
			payload.put("roleName", name);
			result.setPayload(payload);
			playerSession.setChannel(ctx.channel());
			playerSession.sendMessage(new TextWebSocketFrame(JsonUtil.toJson(result)));
		}else if(Strings.isNullOrEmpty(sessionId)){
			ctx.channel().writeAndFlush(new TextWebSocketFrame("User isn't registered!"));
		}else{
			nettyRpcCallInvoker.invoke(rpcMethodName, args);
		}
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)
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