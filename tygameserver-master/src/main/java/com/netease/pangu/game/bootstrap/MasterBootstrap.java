package com.netease.pangu.game.bootstrap;

import com.netease.pangu.game.distribution.handler.MasterServerInitializer;
import com.netease.pangu.game.distribution.impl.MasterServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
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

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class MasterBootstrap implements Bootstrap {
    private final static Logger logger = Logger.getLogger(MasterBootstrap.class);
    private Server server;
    @Value("${server.port}")
    private int port = 9001;
    @Value("${server.httpPort}")
    private int httpPort = 8080;

    @Resource
    private MasterServiceImpl appMasterServiceImpl;

    private ConfigurableApplicationContext context;

    @Override
    public void start() {
        if (server != null) {
            try {
                server.start();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        logger.info("Server started, listening on " + port);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            MasterServerInitializer initializer = context.getBean(MasterServerInitializer.class);
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
                MasterBootstrap.this.stop();
                logger.info("*** server shut down");
            }
        });
    }

    @Override
    public void init(ConfigurableApplicationContext context) {
        server = ServerBuilder.forPort(port).addService(appMasterServiceImpl).build();
        this.context = context;

    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("tygameserver-master-service.xml");
        MasterBootstrap bootstrap = context.getBean(MasterBootstrap.class);
        if (args.length == 2) {
            int port = Integer.parseInt(args[0]);
            bootstrap.setPort(port);
            int httpPort = Integer.parseInt(args[1]);
            bootstrap.setHttpPort(httpPort);
        }
        bootstrap.init(context);
        bootstrap.start();
        try {
            bootstrap.blockUntilShutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
