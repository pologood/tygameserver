package com.netease.pangu.game.distribution;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import io.grpc.Server;
import io.grpc.ServerBuilder;

@Component
public class AppWorkerBootstrap implements Bootstrap  {
	private final static Logger logger = Logger.getLogger(AppWorkerBootstrap.class);
	private Server server;
	private AppWorker appWorker;
	@Override
	public void init() {
		server = ServerBuilder.forPort(appWorker.getPort()).build();
		logger.info("Server started, listening on " + appWorker.getPort());
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
		if(server != null){
			server.shutdown();
		}
	}

	@Override
	public void start() {
		if(server != null){
			try {
				server.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
