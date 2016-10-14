package com.netease.pangu.game.util;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;

public class NettyHttpUtil {
	public static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res){
		if(res.status() ==  HttpResponseStatus.OK){
			HttpUtil.setContentLength(res, res.content().readableBytes());
		}
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if(!HttpUtil.isKeepAlive(req) || res.status() == HttpResponseStatus.OK){
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	public static String getWebSocketLocation(FullHttpRequest req, String webSocketPath) {
        String location =  req.headers().get(HttpHeaderNames.HOST) + webSocketPath;
        return "ws://" + location;
    }
}
