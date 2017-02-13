package com.netease.pangu.game.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by huangc on 2017/2/13.
 */
@ChannelHandler.Sharable
@Lazy
@Component
public class LoginHandler extends ChannelInboundHandlerAdapter {
    @Resource
    private AutowireCapableBeanFactory beanFactory;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if(auth(msg)){
          ctx.pipeline().addAfter("loginHandler","nodeServerHandler", beanFactory.getBean(NodeServerHandler.class));
          ctx.pipeline().remove("loginHandler");
          ctx.fireChannelRead(msg);
      }
    }
    private boolean auth(Object msg){
        return true;
    }
}
