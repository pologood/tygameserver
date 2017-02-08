package com.netease.pangu.game.netty;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.constant.GameServerConst;
import com.netease.pangu.game.http.HttpRequestInvoker;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.rpc.WsRpcCallInvoker;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.AvatarSessionService;
import com.netease.pangu.game.util.JsonUtil;
import com.netease.pangu.game.util.NettyHttpUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

@Sharable
@Lazy
@Component
public class NodeServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(NodeServerHandler.class);

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
        System.out.println("#1 " + ctx.channel().id().asLongText());
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg, GameServerConst.WEB_SOCKET_PATH);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String dataStr = ((TextWebSocketFrame) frame).text();
            Map<String, Object> data = JsonUtil.fromJson(dataStr);
            String rpcMethod = (String) data.get("rpcMethod");

            String uuid = (String) data.get("uuid");
            Double tmp = NumberUtils.toDouble(data.get("gameId").toString());
            long gameId = tmp.longValue();
            if(!wsRpcCallInvoker.containsURIPath(gameId, rpcMethod)) {
                NettyHttpUtil.sendWsResponse(rpcMethod, ctx.channel(), "rpcMethod not exist!");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> args = (Map<String, Object>) data.get("params");
            GameContext<AvatarSession<Avatar>> context = null;
            //TODO need to optimize
            Avatar avatar = avatarSessionService.getAvatarFromCache(gameId, uuid);
            if (avatar == null) {
                avatar = avatarService.getAvatarByUUID(gameId, uuid);
            }
            if (avatar != null) {
                AvatarSession<Avatar> session = avatarSessionService.getSession(avatar.getAvatarId());
                if (session == null) {
                    session = avatarSessionService.createAvatarSession(avatar, ctx.channel());
                }
                if (session.getChannel() == null || !session.getChannel().isActive()) {
                    session.setChannel(ctx.channel());
                }
                context = new GameContext<AvatarSession<Avatar>>(ctx, session, rpcMethod, frame);
                wsRpcCallInvoker.invoke(gameId, rpcMethod, args, context);
            } else {
                NettyHttpUtil.sendWsResponse(rpcMethod, ctx.channel(), "avatar not init");
            }
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
                if (!params.containsKey("gameId")) {
                    NettyHttpUtil.sendHttpResponse(ctx, request,
                            new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST,
                                    Unpooled.copiedBuffer("parameter gameId not exist!", Charset.forName("UTF-8"))));
                }
                Double dGameId = NumberUtils.toDouble(params.get("gameId").toString());
                long gameId = dGameId.longValue();
                URI uri = URI.create(request.uri());
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
                if (httpRequestInvoker.containsURIPath(gameId, uri.getPath())) {
                    httpRequestInvoker.invoke(gameId, uri.getPath(), params, request, response);
                } else {
                    NettyHttpUtil.setHttpResponse(response, HttpResponseStatus.BAD_REQUEST, "uri not exist!");
                }
                NettyHttpUtil.sendHttpResponse(ctx, request, response);
                return;
            }

        } else {
            NettyHttpUtil.sendHttpResponse(ctx, request,
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
            return;
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("#2 " + ctx.channel().id().asLongText());
        cause.printStackTrace();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("#3 " + ctx.channel().id().asLongText());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("#4 " + ctx.channel().id().asLongText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("#5 " + ctx.channel().id().asLongText());
        avatarSessionService.updateAvatarSessionToNotConnectedByChannelId(ctx.channel().id());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("#6 " + ctx.channel().id().asLongText());
    }
}
