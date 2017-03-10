package com.netease.pangu.game.util;

import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.rpc.WsRpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NettyHttpUtil {
    public static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        try {
            if (res.status() == HttpResponseStatus.OK) {
                HttpUtil.setContentLength(res, res.content().readableBytes());
            }

            if (ctx.channel().isActive()) {
                ChannelFuture f = ctx.channel().writeAndFlush(res);

                if (!HttpUtil.isKeepAlive(req) || res.status() == HttpResponseStatus.OK) {

                    f.addListener(ChannelFutureListener.CLOSE);
                }
            }
        } finally {
            if (res.content().refCnt() > 0) {
                res.content().release();
            }
        }
    }

    public static void sendWsResponse(String rpcMethodName, Channel channel, Object content) {
        WsRpcResponse response = WsRpcResponse.create(rpcMethodName);
        response.setContent(content);
        sendWsResponse(rpcMethodName, channel, response);
    }

    public static void sendWsResponse(@SuppressWarnings("rawtypes") GameContext context, Channel channel, Object content) {
        sendWsResponse(context.getRpcMethod(), channel, content);
    }

    public static void sendWsResponse(@SuppressWarnings("rawtypes") GameContext context, Object content) {
        sendWsResponse(context.getRpcMethod(), context.getChannel(), content);
    }

    public static void sendWsResponse(String rpcMethodName, Channel channel, WsRpcResponse response) {
        if (channel.isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(response)));
        }
    }

    public static void sendWsResponse(@SuppressWarnings("rawtypes") GameContext context, WsRpcResponse response) {
        sendWsResponse(context.getRpcMethod(), context.getChannel(), response);
    }

    public static String getWebSocketLocation(FullHttpRequest req, String webSocketPath) {
        String location = req.headers().get(HttpHeaderNames.HOST) + webSocketPath;
        return "ws://" + location;
    }

    public static String resolveUrlPath(String path) {
        return path.replace("//", "/");
    }

    public static String resolveStartWithEscape(String path) {
        if (!path.isEmpty()) {
            return path.startsWith("/") ? path : "/" + path;
        } else {
            return path;
        }
    }

    public static Map<String, String> parseRequest(FullHttpRequest request) throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        if (request.method() == HttpMethod.GET) {
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            for (Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
                params.put(entry.getKey(), entry.getValue().get(0));
            }
        } else if (request.method() == HttpMethod.POST) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
            decoder.offer(request);
            List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
            for (InterfaceHttpData parm : parmList) {
                Attribute data = (Attribute) parm;
                params.put(data.getName(), data.getValue());
            }
        } else {
            throw new UnsupportedOperationException("http method not support");
        }

        return params;
    }

    public static FullHttpResponse createBadRequestResponse() {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
    }

    public static FullHttpResponse createHttpResponse(HttpResponseStatus status, String msg) {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(msg, Charset.forName("UTF-8")));
    }

    public static void setHttpResponse(FullHttpResponse response, HttpResponseStatus status) {
        response.setStatus(status);
    }


    public static void setHttpResponse(FullHttpResponse response, HttpResponseStatus status, String msg) {
        response.setStatus(status);
        response.content().writeBytes(Unpooled.copiedBuffer(msg, Charset.forName("UTF-8")));
    }

    public static Set<Cookie> getCookies(FullHttpRequest request) {
        return getCookies(request.headers());
    }

    public static String getCookieValue(FullHttpRequest request, String cookieName, String defaultValue) {
        Set<Cookie> cookies = getCookies(request);
        for (Cookie cookie : cookies) {
            if (cookie.name().equals(cookieName)) {
                return cookie.value();
            }
        }
        return defaultValue;
    }

    public static Set<Cookie> getCookies(FullHttpResponse response) {
        return getCookies(response.headers());
    }

    private static Set<Cookie> getCookies(HttpHeaders headers) {
        Set<Cookie> cookies;
        String value = headers.get(HttpHeaderNames.COOKIE);
        if (value == null) {
            /**
             * Returns an empty set (immutable).
             */
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.STRICT.decode(value);
        }
        return cookies;
    }

    public static void setCookie(FullHttpResponse response, Cookie cookie) {
        Set<Cookie> cookies = new HashSet<Cookie>(getCookies(response));
        cookies.add(cookie);
        response.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookies));
    }

}
