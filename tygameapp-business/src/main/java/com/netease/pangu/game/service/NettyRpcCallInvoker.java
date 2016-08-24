package com.netease.pangu.game.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.netease.pangu.game.common.NettyRpcCall;
import com.netease.pangu.game.common.NettyRpcController;

@Component
public class NettyRpcCallInvoker {
	private Map<String, Object> controllerMap;
	private Map<String, Method> methodMap;
	private Map<String, String> nettyRpcCallAnnoValueMap;
	@Resource
	private ConfigurableListableBeanFactory beanFactory;
	public static class NettyRpcCallException extends RuntimeException {
		private static final long serialVersionUID = -169844953289757522L;

		public NettyRpcCallException(String msg) {
			super(msg);
		}

		public NettyRpcCallException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	private Method getMethod(String rpcMethodName){
		return methodMap.get(rpcMethodName);
	}
	
	private Object getController(String rpcMethodName){
		return controllerMap.get(nettyRpcCallAnnoValueMap.get(rpcMethodName));
		
	}
	
	public Object invoke(String rpcMethodName, Map<String, Object> params){
		Method method = getMethod(rpcMethodName);
		Class<?>[] paramTypes = method.getParameterTypes();
		List<Object> args = new ArrayList<Object>();
		for(Class<?> pClazz: paramTypes){
			String paramName = pClazz.getName();
			args.add(params.get(paramName));
		}
		Object controller = getController(rpcMethodName);
		try {
			return method.invoke(controller, args.toArray(new Object[0]));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@PostConstruct
	public void init() {
		methodMap = new HashMap<String, Method>();
		nettyRpcCallAnnoValueMap = new HashMap<String, String>();
		controllerMap = beanFactory.getBeansWithAnnotation(NettyRpcController.class);
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
		NettyRpcCallInvoker manager = new NettyRpcCallInvoker();
		manager.init();
	}
}
