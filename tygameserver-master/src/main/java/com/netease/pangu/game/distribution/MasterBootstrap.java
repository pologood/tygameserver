package com.netease.pangu.game.distribution;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.netease.pangu.game.distribution.impl.AppMasterServiceImpl;

import io.grpc.Server;
import io.grpc.ServerBuilder;

@Component
public class MasterBootstrap implements Bootstrap {
	private final static Logger logger = Logger.getLogger(MasterBootstrap.class);
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
	public void init() {
		server = ServerBuilder.forPort(port).addService(appMasterServiceImpl).build();
		logger.info("Server started, listening on " + port);
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
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("tygameserver-master.xml");
		MasterBootstrap bootstrap = context.getBean(MasterBootstrap.class);
		int port = Integer.parseInt(args[0]);
		bootstrap.setPort(port);
		bootstrap.init();
		bootstrap.start();
		try {
			bootstrap.blockUntilShutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
