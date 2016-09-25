package com.netease.pangu.game.distribution.handler;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.netease.pangu.game.util.NettyHttpUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

@Sharable
@Lazy
@Component
public class MasterHandler extends ChannelInboundHandlerAdapter {
	private static String WEB_SOCKET_PATH = "websocket";

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

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame req) {

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
				//TODO 
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
