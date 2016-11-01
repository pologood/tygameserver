package com.netease.pangu.game.distribution;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.annotation.Resource;
import javax.net.ssl.SSLException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.netease.pangu.game.distribution.handler.MasterServerInitializer;
import com.netease.pangu.game.distribution.impl.AppMasterServiceImpl;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

@Component
public class MasterBootstrap implements Bootstrap {
	private final static Logger logger = Logger.getLogger(MasterBootstrap.class);
	private static final boolean SSL = System.getProperty("ssl") != null;
	private static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));
	private Server server;
	@Value("${server.port}")
	private int port = 9001;
	@Resource
	private AppMasterServiceImpl appMasterServiceImpl;

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
	}

	@Override
	public void init(ConfigurableApplicationContext context) {
		server = ServerBuilder.forPort(port).addService(appMasterServiceImpl).build();
		logger.info("Server started, listening on " + port);
		SslContext sslCtx;
		if (SSL) {
			try {
				SelfSignedCertificate ssc = new SelfSignedCertificate();
				sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
				
			} catch (SSLException | CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally{
				sslCtx = null;
			}
			
		} else {
			sslCtx = null;
		}
		
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			MasterServerInitializer initializer = context.getBean(MasterServerInitializer.class);
			initializer.setSslCtx(sslCtx);
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(initializer);

			Channel ch = b.bind(PORT).sync().channel();
			ch.closeFuture().sync();
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
				MasterBootstrap.this.stop();
				logger.info("*** server shut down");
			}
		});
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
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("tygameserver-master-service.xml");
		MasterBootstrap bootstrap = context.getBean(MasterBootstrap.class);
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
