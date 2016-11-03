package com.netease.pangu.game.distribution;

import java.io.IOException;
import java.net.InetAddress;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.net.ssl.SSLException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.netease.pangu.distribution.proto.RpcResponse;
import com.netease.pangu.game.distribution.handler.AppWorkerServerInitializer;
import com.netease.pangu.game.service.GameRoomManager;
import com.netease.pangu.game.service.PlayerManager;

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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

@Component
public class AppWorkerBootstrap implements Bootstrap {
	private final static Logger logger = Logger.getLogger(AppWorkerBootstrap.class);
	private static final boolean SSL = System.getProperty("ssl") != null;
	private static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8181" : "8081"));
	private Server server;
	private ConfigurableApplicationContext context;
	@Value("${server.port}")
	private int port = 9002;

	@Value("${server.name}")
	private String name;

	@Value("${master.port}")
	private int masterPort;

	@Value("${master.ip}")
	private String masterIp;

	@Resource
	private AppMasterCallService appMasterCallService;

	@Resource
	private GameRoomManager gameRoomManager;

	@Resource
	private PlayerManager playerManager;
	
	private AppWorker worker;

	@Override
	public void init(ConfigurableApplicationContext context) {
		server = ServerBuilder.forPort(port).build();
		this.context = context;
		logger.info("Server started, listening on " + port);

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
				worker = new AppWorker();
				worker.setIp(InetAddress.getLocalHost().getHostAddress());
				worker.setName(InetAddress.getLocalHost().getHostName());
				worker.setPort(PORT);
				playerManager.setCurrentAppWorker(worker);
				appMasterCallService.init(masterIp, masterPort);
				logger.info("app worker init");
				ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
				service.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						worker.setCount(gameRoomManager.getRooms().size());
						RpcResponse response = appMasterCallService.addOrUpdateWorker(worker);
						logger.info(response.getMessage());
					}
				}, 3, 3, TimeUnit.SECONDS);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		SslContext sslCtx;
		if (SSL) {
			try {
				SelfSignedCertificate ssc = new SelfSignedCertificate();
				sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();

			} catch (SSLException | CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				sslCtx = null;
			}

		} else {
			sslCtx = null;
		}

		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			AppWorkerServerInitializer initializer = context.getBean(AppWorkerServerInitializer.class);
			initializer.setSslCtx(sslCtx);
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(initializer);

			Channel ch = b.bind(PORT).sync().channel();
			ChannelFuture future = ch.closeFuture();
			future.sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
				AppWorkerBootstrap.this.stop();
				logger.info("*** server shut down");
			}
		});
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("tygameserver-slave-service.xml");
		AppWorkerBootstrap bootstrap = context.getBean(AppWorkerBootstrap.class);
		int port = Integer.parseInt(args[0]);
		bootstrap.setPort(port);
		bootstrap.init(context);
		bootstrap.start();
		try {
			bootstrap.blockUntilShutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
