package com.netease.pangu.game.distribution.handler;

import io.netty.channel.*;
import io.netty.channel.socket.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.*;
import io.netty.handler.ssl.*;
import org.springframework.beans.factory.config.*;
import org.springframework.stereotype.*;

import javax.annotation.*;

@Component
public class MasterServerInitializer extends ChannelInitializer<SocketChannel> {

private static final String WEBSOCKET_PATH = "/ws";
    @Resource
    private AutowireCapableBeanFactory beanFactory;
	
	private SslContext sslCtx;
    
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx!= null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
 
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
        pipeline.addLast(beanFactory.getBean(MasterServerHandler.class));
    }

	public SslContext getSslCtx() {
		return sslCtx;
	}

	public void setSslCtx(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}

}
