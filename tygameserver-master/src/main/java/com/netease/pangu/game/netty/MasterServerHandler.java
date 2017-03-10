package com.netease.pangu.game.netty;

import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.constant.GameServerConst;
import com.netease.pangu.game.http.HttpRequestInvoker;
import com.netease.pangu.game.rpc.WsRpcCallInvoker;
import com.netease.pangu.game.util.BusinessCode;
import com.netease.pangu.game.util.JsonUtil;
import com.netease.pangu.game.util.NettyHttpUtil;
import com.netease.pangu.game.util.UrsAuthUtils;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
public class MasterServerHandler extends ChannelInboundHandlerAdapter {
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
            @SuppressWarnings("unchecked")
            Map<String, Object> args = (Map<String, Object>) data.get("params");
            Double tmp = NumberUtils.toDouble(data.get("moduleId").toString());
            long moduleId = tmp.longValue();
            GameContext<Void> context = new GameContext<Void>(moduleId, ctx, null, rpcMethod, frame);
            if (wsRpcCallInvoker.containsURIPath(moduleId, rpcMethod)) {
                wsRpcCallInvoker.invoke(moduleId, rpcMethod, args, context);
            } else if (wsRpcCallInvoker.containsURIPath(GameConst.SYSTEM, rpcMethod)) {
                wsRpcCallInvoker.invoke(GameConst.SYSTEM, rpcMethod, args, context);
            } else {
                NettyHttpUtil.sendWsResponse(context, "rpcMethod not exist!");
            }
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request, String webSocketPath) throws IOException {
        if (!request.decoderResult().isSuccess()) {
            NettyHttpUtil.sendHttpResponse(ctx, request,
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        if (request.method() == HttpMethod.GET) {
            URI uri = URI.create(request.uri());
            if (uri.getPath().equalsIgnoreCase(webSocketPath)) {
                WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                        NettyHttpUtil.getWebSocketLocation(request, webSocketPath), null, true, GameConst.maxFramePayloadLength);
                WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
                if (handshaker == null) {
                    WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                } else {
                    handshaker.handshake(ctx.channel(), request);
                }
            } else {
                Map<String, String> params = NettyHttpUtil.parseRequest(request);
                if (!params.containsKey("moduleId")) {
                    NettyHttpUtil.sendHttpResponse(ctx, request,
                            new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND,
                                    Unpooled.copiedBuffer("parameter moduleId not exist!", Charset.forName("UTF-8"))));
                } else {
                    Double tmp = NumberUtils.toDouble(params.get("moduleId").toString());
                    long moduleId = tmp.longValue();
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
                    String userName = UrsAuthUtils.getLoginedUserName(request, response);
                    if (httpRequestInvoker.containsURIPath(moduleId, uri.getPath())) {
                        if (httpRequestInvoker.isNeedAuth(moduleId, uri.getPath())) {
                            if (StringUtils.isEmpty(userName)) {
                                NettyHttpUtil.setHttpResponse(response, HttpResponseStatus.OK, JsonUtil.toJson(BusinessCode.failed(BusinessCode.NOT_LOGINED)));
                                NettyHttpUtil.sendHttpResponse(ctx, request, response);
                                return;
                            }
                        }
                        httpRequestInvoker.invoke(moduleId, uri.getPath(), params, request, response);
                    } else if (httpRequestInvoker.containsURIPath(GameConst.SYSTEM, uri.getPath())) {
                        if (httpRequestInvoker.isNeedAuth(GameConst.SYSTEM, uri.getPath())) {
                            if (StringUtils.isEmpty(userName)) {
                                NettyHttpUtil.setHttpResponse(response, HttpResponseStatus.OK, JsonUtil.toJson(BusinessCode.failed(BusinessCode.NOT_LOGINED)));
                                NettyHttpUtil.sendHttpResponse(ctx, request, response);
                                return;
                            }
                        }
                        httpRequestInvoker.invoke(GameConst.SYSTEM, uri.getPath(), params, request, response);

                    } else {
                        NettyHttpUtil.setHttpResponse(response, HttpResponseStatus.NOT_FOUND, "uri not exist!");
                    }
                    NettyHttpUtil.sendHttpResponse(ctx, request, response);
                }
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
        cause.printStackTrace();
    }
}
