package com.netease.pangu.game.netty;

import com.netease.pangu.game.http.HttpRequestInvoker;
import com.netease.pangu.game.util.NettyHttpUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by huangc on 2017/2/6.
 */

@ChannelHandler.Sharable
@Lazy
@Component
public class AuthServerHandler extends ChannelInboundHandlerAdapter {
    @Resource
    private HttpRequestInvoker httpRequestInvoker;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws IOException {
        if (!request.decoderResult().isSuccess()) {
            NettyHttpUtil.sendHttpResponse(ctx, request,
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        if (request.method() == HttpMethod.GET) {
            Map<String, String> params = NettyHttpUtil.parseRequest(request);
            if (!params.containsKey("gameId")) {
                NettyHttpUtil.sendHttpResponse(ctx, request,
                        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST,
                                Unpooled.copiedBuffer("parameter gameId not exist!", Charset.forName("UTF-8"))));
                return;
            }
            URI uri = URI.create(request.uri());
            Double tmp = NumberUtils.toDouble(params.get("gameId").toString());
            long gameId = tmp.longValue();
            if (httpRequestInvoker.containsURIPath(gameId, uri.getPath())) {
                FullHttpResponse response = httpRequestInvoker.invoke(gameId, uri.getPath(), params, request);
                NettyHttpUtil.sendHttpResponse(ctx, request, response);
            } else {
                NettyHttpUtil.sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("uri not exist!", Charset.forName("UTF-8"))));
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
