package com.netease.pangu.game.distribution.handler;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.http.HttpRequestInvoker;
import com.netease.pangu.game.rpc.WsRpcCallInvoker;
import com.netease.pangu.game.util.JsonUtil;
import com.netease.pangu.game.util.NettyHttpUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
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
	private HttpRequestInvoker httpRequestInvoker;
	
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
			@SuppressWarnings("unchecked")
			Map<String, Object> args = (Map<String, Object>)data.get("params");
			Double tmp = NumberUtils.toDouble(data.get("gameId").toString());
			long gameId = tmp.longValue();
			GameContext<Void> context = new GameContext<Void>(ctx, null, rpcMethodName, frame);
			wsRpcCallInvoker.invoke(gameId, rpcMethodName, args, context);
		}
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request, String webSocketPath) throws IOException {
		if (!request.decoderResult().isSuccess()) {
			NettyHttpUtil.sendHttpResponse(ctx, request,
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}

		if (request.method() == HttpMethod.GET) {
			if (request.uri().equalsIgnoreCase(webSocketPath)) {
				WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
						NettyHttpUtil.getWebSocketLocation(request, webSocketPath), null, true);
				WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
				if (handshaker == null) {
					WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
				} else {
					handshaker.handshake(ctx.channel(), request);
				}
			}else{
				Map<String, String> params = NettyHttpUtil.parseRequest(request);
				if(!params.containsKey("gameId")){
					NettyHttpUtil.sendHttpResponse(ctx, request,
							new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST,
									Unpooled.copiedBuffer("parameter gameId not exist!", Charset.forName("UTF-8"))));
					return;
				}
				URI uri = URI.create(request.uri());	
				Double tmp = NumberUtils.toDouble(params.get("gameId").toString());
				long gameId = tmp.longValue();
				if(httpRequestInvoker.containsURIPath(gameId, uri.getPath())){
					FullHttpResponse result = httpRequestInvoker.invoke(gameId, uri.getPath(), params, request);
					NettyHttpUtil.sendHttpResponse(ctx, request, result);
				}else{
					NettyHttpUtil.sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("uri not exist!",Charset.forName("UTF-8"))));
				}
			}
			
		}else{
			NettyHttpUtil.sendHttpResponse(ctx, request,
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
			return;
		}
		
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}
}
