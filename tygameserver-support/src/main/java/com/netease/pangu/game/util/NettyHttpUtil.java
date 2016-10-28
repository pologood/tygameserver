package com.netease.pangu.game.util;

import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.rpc.WsRpcResponse;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

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
	
	public static void sendWsResponse(GameContext context, Channel channel, Object content){
		WsRpcResponse response = WsRpcResponse.create(context.getRpcMethodName());
		response.setContent(content);
		channel.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(response)));
	}
	
	public static void sendWsResponse(GameContext context, WsRpcResponse response){
		context.getChannel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(response)));
	}
	
	public static String getWebSocketLocation(FullHttpRequest req, String webSocketPath) {
        String location =  req.headers().get(HttpHeaderNames.HOST) + webSocketPath;
        return "ws://" + location;
    }
	
	public static String resolveUrlPath(String path){
		return path.replace("//", "/");
	}
	
	public static String resolveStartWithEscape(String path){
		if(!path.isEmpty()){
			return path.startsWith("/")? path: "/" + path;
		} else{
			return path;
		} 
	}
}
