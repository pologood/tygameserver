package com.netease.pangu.game.handler.websocket;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.netease.pangu.game.meta.PlayerSession;
import com.netease.pangu.game.service.NettyRpcCallInvoker;
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

	public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
            TextWebSocketFrame frame) throws Exception { 
        String result = ((TextWebSocketFrame) frame).text();
		Map<String, Object> obj = JsonUtil.fromJson(result);
		String rpcMethodName = (String) obj.get("rpcMethod");
		
		@SuppressWarnings("unchecked")
		Map<String, Object> params = (Map<String, Object>) obj.get("params");
		if(rpcMethodName.equals("create")){
			PlayerSession playerSession = (PlayerSession)nettyRpcCallInvoker.invoke(rpcMethodName, params);
			playerSession.setChannel(ctx.channel());
		}
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)
        Channel incoming = ctx.channel();
        // Broadcast a message to multiple Channels
        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " added "));

        channels.add(incoming);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
        Channel incoming = ctx.channel();

        // Broadcast a message to multiple Channels
        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " leave"));
        }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
        Channel incoming = ctx.channel();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        Channel incoming = ctx.channel();
        cause.printStackTrace();
        ctx.close();
    }
}