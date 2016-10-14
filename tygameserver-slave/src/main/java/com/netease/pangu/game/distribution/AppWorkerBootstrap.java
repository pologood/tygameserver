package com.netease.pangu.game.distribution;

import java.io.IOException;
import java.net.InetAddress;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import io.grpc.Server;
import io.grpc.ServerBuilder;

@Component
public class AppWorkerBootstrap implements Bootstrap {
	private final static Logger logger = Logger.getLogger(AppWorkerBootstrap.class);
	private Server server;
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
	
	@Override
	public void init(ConfigurableApplicationContext context) {
		server = ServerBuilder.forPort(port).build();
		logger.info("Server started, listening on " + port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("*** shutting down server since JVM is shutting down");
				AppWorkerBootstrap.this.stop();
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

	@Override
	public void start() {
		if (server != null) {
			try {
				server.start();
				AppWorker worker = new AppWorker();
				worker.setIp(InetAddress.getLocalHost().getHostAddress());
				worker.setName(InetAddress.getLocalHost().getHostName());
				worker.setPort(port);
				appMasterCallService.init(masterIp, masterPort);
				appMasterCallService.addWorker(worker);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("tygameserver-slave.xml");
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
