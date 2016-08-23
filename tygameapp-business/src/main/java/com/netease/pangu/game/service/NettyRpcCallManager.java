package com.netease.pangu.game.service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.netease.pangu.game.common.NettyRpcCall;
import com.netease.pangu.game.common.NettyRpcController;
import com.netease.pangu.game.config.TyGameAppConfig;

@Component
public class NettyRpcCallManager {
	private AnnotationConfigApplicationContext annotationConfigApplicationContext;
	private Map<String, Object> controllerMap;
	private Map<String, Method> methodMap;
	private Map<String, String> nettyRpcCallAnnoValueMap;
	public static class NettyRpcCallException extends RuntimeException {
		private static final long serialVersionUID = -169844953289757522L;

		public NettyRpcCallException(String msg) {
			super(msg);
		}

		public NettyRpcCallException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public void init() {
		methodMap = new HashMap<String, Method>();
		nettyRpcCallAnnoValueMap = new HashMap<String, String>();
		annotationConfigApplicationContext = new AnnotationConfigApplicationContext(TyGameAppConfig.class);
		controllerMap = annotationConfigApplicationContext.getBeansWithAnnotation(NettyRpcController.class);
		for (Entry<String, Object> entry : controllerMap.entrySet()) {
			initAndCheckMethodsByNettyRpcCall(entry.getKey(), entry.getValue().getClass());
		}
	}

	public void initAndCheckMethodsByNettyRpcCall(String controllerName, Class<?> clazz){
		Class<?> currentClazz = clazz;
		while(currentClazz != Object.class){
			Method[] methods = currentClazz.getDeclaredMethods();
			for(final Method method: methods){
				if(method.getModifiers() == Modifier.PUBLIC && method.isAnnotationPresent(NettyRpcCall.class)){
					NettyRpcCall anno = method.getAnnotation(NettyRpcCall.class);
					if(Strings.isNullOrEmpty(anno.value())){
						throw new NettyRpcCallException("NettyRpcCall value can't be empty");
					}
					if(methodMap.containsKey(anno.value())){
						throw new NettyRpcCallException("NettyRpcCall value must be unique");
					}
					methodMap.put(anno.value(), method);
					nettyRpcCallAnnoValueMap.put(anno.value(), controllerName);
				}
			}
			currentClazz = currentClazz.getSuperclass();
		}
	}

	public static void main(String[] args) {
		NettyRpcCallManager manager = new NettyRpcCallManager();
		manager.init();
	}
}
