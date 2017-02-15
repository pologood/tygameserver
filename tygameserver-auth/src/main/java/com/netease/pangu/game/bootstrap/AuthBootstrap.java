package com.netease.pangu.game.bootstrap;

import com.netease.pangu.game.netty.AuthServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by huangc on 2017/2/6.
 */
@Component
public class AuthBootstrap implements Bootstrap {
    private final static Logger logger = Logger.getLogger(AuthBootstrap.class);
    @Value("${server.httpPort}")
    private int httpPort = 8100;
    private ConfigurableApplicationContext context;

    @Override
    public void init(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            AuthServerInitializer initializer = context.getBean(AuthServerInitializer.class);
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO)).childHandler(initializer);

            Channel ch = b.bind(httpPort).sync().channel();
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            context.close();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("*** shutting down server since JVM is shutting down");
                AuthBootstrap.this.stop();
                logger.info("*** server shut down");
            }
        });
    }

    @Override
    public void stop() {

    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("tygameserver-auth-service.xml");
        AuthBootstrap bootstrap = context.getBean(AuthBootstrap.class);
        if (args.length == 3) {
            int httpPort = Integer.parseInt(args[0]);
            int rpcPort = Integer.parseInt(args[1]);
            String name = args[2];
            bootstrap.setHttpPort(httpPort);
        }
        bootstrap.init(context);
        bootstrap.start();
    }
}
