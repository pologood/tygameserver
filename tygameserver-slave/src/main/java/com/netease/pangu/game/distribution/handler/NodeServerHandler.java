package com.netease.pangu.game.distribution.handler;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.http.HttpRequestInvoker;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.rpc.WsRpcCallInvoker;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.AvatarSessionService;
import com.netease.pangu.game.util.JsonUtil;
import com.netease.pangu.game.util.NettyHttpUtil;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.ReturnUtils.GameResult;

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
public class NodeServerHandler extends ChannelInboundHandlerAdapter {
	private static String WEB_SOCKET_PATH = "ws";
	@Resource
	private WsRpcCallInvoker wsRpcCallInvoker;
	@Resource
	private HttpRequestInvoker httpRequestInvoker;
	@Resource
	private AvatarSessionService avatarSessionService;

	@Resource
	private AvatarService avatarService;

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
			String uuid = (String)data.get("uuid");
			long gameId = (Long)data.get("gameId");
			@SuppressWarnings("unchecked")
			Map<String, Object> args = (Map<String, Object>)data.get("params");
			GameContext<AvatarSession<Avatar>> context = null;
			Avatar avatar = avatarService.getAvatarByUUID(gameId, uuid);
			AvatarSession<Avatar> session = avatarSessionService.getSession(avatar.getAvatarId());
			if (session == null) {
				session = avatarSessionService.createAvatarSession(avatar, ctx.channel());
			}
			if(session.getChannel() == null || !session.getChannel().isActive()){
				session.setChannel(ctx.channel());
			}
			context = new GameContext<AvatarSession<Avatar>>(ctx, session, rpcMethodName, frame);
			wsRpcCallInvoker.invoke(rpcMethodName, args, context);
		}
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request, String webSocketPath)
			throws IOException {
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
			} else {
				Map<String, String> params = NettyHttpUtil.parseRequest(request);
				URI uri = URI.create(request.uri());
				if (httpRequestInvoker.containsURIPath(uri.getPath())) {
					FullHttpResponse result = httpRequestInvoker.invoke(uri.getPath(), params, request);
					NettyHttpUtil.sendHttpResponse(ctx, request, result);
				} else {
					NettyHttpUtil.sendHttpResponse(ctx, request,
							new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST,
									Unpooled.copiedBuffer("uri not exist!", Charset.forName("UTF-8"))));
				}
			}

		} else {
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