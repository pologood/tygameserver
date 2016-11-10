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
import com.netease.pangu.game.distribution.handler.NodeServerInitializer;
import com.netease.pangu.game.distribution.service.SystemAttrService;
import com.netease.pangu.game.service.RoomService;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.AvatarSessionService;

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
public class NodeBootstrap implements Bootstrap {
	private final static Logger logger = Logger.getLogger(NodeBootstrap.class);
	private static final boolean SSL = System.getProperty("ssl") != null;
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
	private MasterCallService appMasterCallService;

	@Resource
	private RoomService gameRoomManager;

	@Resource
	private SystemAttrService systemAttrService;
	
	@Resource
	private AvatarSessionService playerSessionManager;
	
	private Node node;

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
				node = new Node();
				node.setIp(InetAddress.getLocalHost().getHostAddress());
				node.setHostName(InetAddress.getLocalHost().getHostName());
				node.setName(name);
				node.setPort(httpPort);
				systemAttrService.setNode(node);
				appMasterCallService.init(masterIp, masterPort);
				logger.info("app worker init");
				ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
				service.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						try{
							node.setCount(playerSessionManager.getSessions().size());
							RpcResponse response = appMasterCallService.addOrUpdateNode(node);
						logger.info(response.getMessage());
						}catch(Exception e){
							e.printStackTrace();
						}
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
			NodeServerInitializer initializer = context.getBean(NodeServerInitializer.class);
			initializer.setSslCtx(sslCtx);
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
				NodeBootstrap.this.stop();
				logger.info("*** server shut down");
			}
		});
	}

	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("tygameserver-slave-service.xml");
		NodeBootstrap bootstrap = context.getBean(NodeBootstrap.class);
		int httpPort = Integer.parseInt(args[0]);
		int rpcPort = Integer.parseInt(args[1]);
		String name = args[2];
		bootstrap.setHttpPort(httpPort);
		bootstrap.setRpcPort(rpcPort);
		bootstrap.setName(name);
		bootstrap.init(context);
		bootstrap.start();
		try {
			bootstrap.blockUntilShutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}