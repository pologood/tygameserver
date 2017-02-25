package com.netease.pangu.game.netty;

import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.util.HttpClientUtils;
import com.netease.pangu.game.util.JsonUtil;
import com.netease.pangu.game.util.NettyHttpUtil;
import com.netease.pangu.game.util.ReturnUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.netease.pangu.game.constant.GameServerConst.WEB_SOCKET_PATH;

/**
 * Created by huangc on 2017/2/13.
 */
@ChannelHandler.Sharable
@Lazy
@Component
public class LoginHandler extends ChannelInboundHandlerAdapter {
    @Resource
    private AutowireCapableBeanFactory beanFactory;

    @Value("${auth.url}")
    private String authBaseUrl;

    private final static String VALIDATE_URL = "/auth/validate";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if(auth(msg)){
          ctx.pipeline().addAfter("loginHandler", "nodeServerHandler", beanFactory.getBean(NodeServerHandler.class));
          ctx.pipeline().remove("loginHandler");
          ctx.fireChannelRead(msg);
      }
    }
    private boolean auth(Object msg){
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest)msg;
            URI uri = URI.create(req.uri());
            if(uri.getPath().equals(WEB_SOCKET_PATH)) {
                Map<String, String> uriParams = null;
                try {
                    uriParams = NettyHttpUtil.parseRequest(req);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                String token = MapUtils.getString(uriParams, "token");
                if (StringUtils.isNotEmpty(token)) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("token", token);
                    params.put("gameId", GameConst.SYSTEM);
                    HttpClientUtils.HttpResult result = HttpClientUtils.get(authBaseUrl + VALIDATE_URL, params);
                    ReturnUtils.GameResult gs = JsonUtil.fromJson(result.getContentAsString(), ReturnUtils.GameResult.class);
                    if (gs.getCode() == ReturnUtils.SUCC) {
                        return true;
                    }
                }
            }else{
                return true;
            }
        }
        return false;

    }
}
