package com.netease.pangu.game.netty;

import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.constant.GameServerConst;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.SecureWebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class NodeServerInitializer extends ChannelInitializer<SocketChannel> {

    @Resource
    private AutowireCapableBeanFactory beanFactory;

    private SslContext sslCtx;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast("ping", new IdleStateHandler(20, 20, 30, TimeUnit.MINUTES));
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(GameConst.maxFramePayloadLength));
        pipeline.addLast("loginHandler", beanFactory.getBean(LoginHandler.class));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new SecureWebSocketServerProtocolHandler(GameServerConst.WEB_SOCKET_PATH, null, true, GameConst.maxFramePayloadLength));

    }

    public SslContext getSslCtx() {
        return sslCtx;
    }

    public void setSslCtx(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

}
