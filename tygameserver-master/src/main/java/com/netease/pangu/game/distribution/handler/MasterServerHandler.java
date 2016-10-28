package com.netease.pangu.game.distribution.handler;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.common.meta.PlayerSession;
import com.netease.pangu.game.rpc.WsRpcCallInvoker;
import com.netease.pangu.game.service.PlayerSessionManager;
import com.netease.pangu.game.util.JsonUtil;
import com.netease.pangu.game.util.NettyHttpUtil;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

@Sharable
@Lazy
@Component
public class MasterServerHandler extends ChannelInboundHandlerAdapter {
	private static String WEB_SOCKET_PATH = "websocket";
	@Resource
	private WsRpcCallInvoker wsRpcCallInvoker;
	@Resource
	private PlayerSessionManager playerSessionManager;
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, (FullHttpRequest) msg, WEB_SOCKET_PATH);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		if(frame instanceof TextWebSocketFrame){
			String dataStr = ((TextWebSocketFrame) frame).text();
			Map<String, Object> data = JsonUtil.fromJson(dataStr);
			String rpcMethodName = (String) data.get("rpcMethod");
			String sessionId = (String) data.get("sessionId");
			@SuppressWarnings("unchecked")
			List<Object> args = (List<Object>) data.get("params");
			GameContext context = null;
			if (Strings.isNullOrEmpty(sessionId)) {
				context = new GameContext(ctx, null, rpcMethodName, frame);
			} else {
				Double num = NumberUtils.toDouble(sessionId);
				long playerSessionId = num.longValue();
				PlayerSession playerSession = playerSessionManager.getSession(playerSessionId);
				if (playerSession != null) {
					context = new GameContext(ctx, playerSession, rpcMethodName, frame);
				} else {
					GameResult result = ReturnUtils.failed(rpcMethodName, "user hasn't registered");
					ctx.channel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(result)));
					return;
				}
			}
			wsRpcCallInvoker.invoke(rpcMethodName, args, context);
		}
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req, String webSocketPath) {
		if (!req.decoderResult().isSuccess()) {
			NettyHttpUtil.sendHttpResponse(ctx, req,
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}

		if (req.method() == HttpMethod.GET) {
			if (req.uri().equalsIgnoreCase(webSocketPath)) {
				WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
						NettyHttpUtil.getWebSocketLocation(req, webSocketPath), null, true);
				WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
				if (handshaker == null) {
					WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
				} else {
					handshaker.handshake(ctx.channel(), req);
				}
			}else{
				NettyHttpUtil.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("hello",Charset.forName("UTF-8"))));
			}
			
		}else{
			NettyHttpUtil.sendHttpResponse(ctx, req,
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
			return;
		}
		
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}
}
