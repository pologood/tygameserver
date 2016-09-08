package com.netease.pangu.game.distribution;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.netease.pangu.game.distribution.impl.AppWorkerManagerImpl;

import io.grpc.Server;
import io.grpc.ServerBuilder;

@Component
public class MasterBootstrap implements Bootstrap {
	private final static Logger logger = Logger.getLogger(MasterBootstrap.class);
	private Server server;
	private AppWorker appWorker;
	@Resource
	private AppWorkerManagerImpl appWorkerManagerImpl;
	
	@Override
	public void start() {
	}
	
	public AppWorker getAppWorker() {
		return appWorker;
	}
	public void setAppNode(AppWorker appWorker) {
		this.appWorker = appWorker;
	}

	@Override
	public void init() {
		server = ServerBuilder.forPort(appWorker.getPort()).addService(appWorkerManagerImpl).build();
		logger.info("Server started, listening on " + appWorker.getPort());
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
		if(server != null){
			server.shutdown();
		}
	}

}
