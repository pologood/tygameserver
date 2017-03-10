package com.netease.pangu.game.bootstrap;

import com.netease.pangu.distribution.proto.RpcResponse;
import com.netease.pangu.game.distribution.MasterCallService;
import com.netease.pangu.game.distribution.Slave;
import com.netease.pangu.game.distribution.service.SystemAttrService;
import com.netease.pangu.game.netty.SlaveServerInitializer;
import com.netease.pangu.game.service.AvatarSessionService;
import com.netease.pangu.game.service.RoomService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SlaveBootstrap implements Bootstrap {
    private final static Logger logger = Logger.getLogger(SlaveBootstrap.class);
    private Server server;
    private ConfigurableApplicationContext context;
    @Value("${server.rpcPort}")
    private int rpcPort = 9002;

    @Value("${server.httpPort}")
    private int httpPort = 8080;


    @Value("${server.name}")
    private String name;

    @Value("${master.port}")
    private int masterPort;

    @Value("${master.ip}")
    private String masterIp;

    @Resource
    private MasterCallService masterCallService;

    @Resource
    private RoomService gameRoomManager;

    @Resource
    private SystemAttrService systemAttrService;

    @Resource
    private AvatarSessionService playerSessionManager;

    private Slave slave;

    public int getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(int rpcPort) {
        this.rpcPort = rpcPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void init(ConfigurableApplicationContext context) {
        server = ServerBuilder.forPort(rpcPort).build();
        this.context = context;
        logger.info("RPC Server started, listening on " + rpcPort);

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

    @Override
    public void start() {
        if (server != null) {
            try {
                server.start();
                slave = new Slave();
                slave.setIp(InetAddress.getLocalHost().getHostAddress());
                slave.setHostName(InetAddress.getLocalHost().getHostName());
                slave.setName(name);
                slave.setPort(httpPort);
                systemAttrService.setSlave(slave);
                masterCallService.init(masterIp, masterPort);
                logger.info("app worker init");
                ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
                service.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            slave.setCount(playerSessionManager.getSessions().size());
                            RpcResponse response = masterCallService.addOrUpdateSlave(slave);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 3, 3, TimeUnit.SECONDS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);
        try {
            ServerBootstrap b = new ServerBootstrap();
            SlaveServerInitializer initializer = context.getBean(SlaveServerInitializer.class);
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO)).childHandler(initializer);

            Channel ch = b.bind(httpPort).sync().channel();
            ChannelFuture future = ch.closeFuture();
            future.sync();
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
                SlaveBootstrap.this.stop();
                logger.info("*** server shut down");
            }
        });
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("tygameserver-slave-service.xml");
        SlaveBootstrap bootstrap = context.getBean(SlaveBootstrap.class);
        if (args.length == 3) {
            int httpPort = Integer.parseInt(args[0]);
            int rpcPort = Integer.parseInt(args[1]);
            String name = args[2];
            bootstrap.setHttpPort(httpPort);
            bootstrap.setRpcPort(rpcPort);
            bootstrap.setName(name);
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
