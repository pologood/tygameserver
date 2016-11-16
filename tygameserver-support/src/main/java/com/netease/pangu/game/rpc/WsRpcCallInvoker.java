package com.netease.pangu.game.rpc;

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

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.rpc.annotation.WsRpcCall;
import com.netease.pangu.game.rpc.annotation.WsRpcController;
import com.netease.pangu.game.util.MethodUtil;
import com.netease.pangu.game.util.NettyHttpUtil;

import javassist.NotFoundException;

@Component
public class WsRpcCallInvoker {
	private final static Logger logger = Logger.getLogger(WsRpcCallInvoker.class);
	private Map<Long, Map<String, Object>> controllerMap;
	private Map<Long, Map<String, Method>> methodMap;
	private Map<Long, Map<String, Map<Integer, String>>> methodParamIndexMap;
	private Map<Long, Map<String, String>> nettyRpcCallAnnoValueMap;
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

	private Method getMethod(long gameId, String rpcMethodName) {
		return methodMap.get(gameId).get(rpcMethodName);
	}

	private Object getController(long gameId, String rpcMethodName) {
		return controllerMap.get(gameId).get(nettyRpcCallAnnoValueMap.get(gameId).get(rpcMethodName));
	}

	@SuppressWarnings("unchecked")
	public void invoke(long gameId, String rpcMethodName, Map<String, Object> args, GameContext<?> context) {
		Method method = getMethod(gameId, rpcMethodName);
		Object controller = getController(gameId, rpcMethodName);
		final Class<?>[] parameterTypes = method.getParameterTypes();
		List<Object> convertedArgs = new ArrayList<Object>();
		Map<Integer, String> paramsIndex = getParamsIndex(gameId, rpcMethodName);
		try {
			for (Integer i = 0; i < parameterTypes.length; i++) {
				if (Long.class.isAssignableFrom(parameterTypes[i]) || long.class.isAssignableFrom(parameterTypes[i])) {
					Object arg = args.get(paramsIndex.get(i));
					if (arg == null) {
						String err = String.format("parameter %s is null", paramsIndex.get(i));
						logger.error(err);
						NettyHttpUtil.sendWsResponse(context, err);
						return;
					}
					Double num = NumberUtils.toDouble(arg.toString());
					convertedArgs.add(num.longValue());
				} else if (Integer.class.isAssignableFrom(parameterTypes[i])
						|| int.class.isAssignableFrom(parameterTypes[i])) {
					Object arg = args.get(paramsIndex.get(i));
					if (arg == null) {
						String err = String.format("parameter %s is null", paramsIndex.get(i));
						logger.error(err);
						NettyHttpUtil.sendWsResponse(context, err);
						return;
					}
					Double num = NumberUtils.toDouble(arg.toString());
					convertedArgs.add(num.intValue());
				} else if (Double.class.isAssignableFrom(parameterTypes[i])
						|| double.class.isAssignableFrom(parameterTypes[i])) {
					Object arg = args.get(paramsIndex.get(i));
					if (arg == null) {
						String err = String.format("parameter %s is null", paramsIndex.get(i));
						logger.error(err);
						NettyHttpUtil.sendWsResponse(context, err);
						return;
					}
					Double num = NumberUtils.toDouble(arg.toString());
					convertedArgs.add(num);
				} else if (Float.class.isAssignableFrom(parameterTypes[i])
						|| float.class.isAssignableFrom(parameterTypes[i])) {
					Object arg = args.get(paramsIndex.get(i));
					if (arg == null) {
						String err = String.format("parameter %s is null", paramsIndex.get(i));
						logger.error(err);
						NettyHttpUtil.sendWsResponse(context, err);
						return;
					}
					Double num = NumberUtils.toDouble(arg.toString());
					convertedArgs.add(num.floatValue());
				} else if (String.class.isAssignableFrom(parameterTypes[i])) {
					Object arg = args.get(paramsIndex.get(i));
					if (arg == null) {
						String err = String.format("parameter %s is null", paramsIndex.get(i));
						logger.error(err);
						NettyHttpUtil.sendWsResponse(context, err);
						return;
					}
					convertedArgs.add(arg.toString());
				} else if (Map.class.isAssignableFrom(parameterTypes[i])) {
					Object arg = args.get(paramsIndex.get(i));
					if (arg == null) {
						String err = String.format("parameter %s is null",  paramsIndex.get(i));
						logger.error(err);
						NettyHttpUtil.sendWsResponse(context, err);
						return;
					}
					convertedArgs.add((Map<String, Object>)arg);
				} 
				else if (GameContext.class.isAssignableFrom(parameterTypes[i])) {
					convertedArgs.add(context);
				}
			}
			Object result = method.invoke(controller, convertedArgs.toArray(new Object[0]));
			if (result != null && !Void.class.isAssignableFrom(result.getClass())) {
				if (!result.getClass().equals(WsRpcResponse.class)) {
					NettyHttpUtil.sendWsResponse(context, context.getChannel(), result);
				} else {
					NettyHttpUtil.sendWsResponse(context, (WsRpcResponse) result);
				}
			}
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
	}

	@PostConstruct
	public void init() {
		methodMap = new HashMap<Long, Map<String, Method>>();
		nettyRpcCallAnnoValueMap = new HashMap<Long, Map<String, String>>();
		methodParamIndexMap = new HashMap<Long, Map<String, Map<Integer, String>>>();
		controllerMap = new HashMap<Long, Map<String,Object>>();
		Map<String, Object> controllers = beanFactory.getBeansWithAnnotation(WsRpcController.class);
		for (Entry<String, Object> entry : controllers.entrySet()) {
			WsRpcController anno = entry.getValue().getClass().getAnnotation(WsRpcController.class);
			if(!controllerMap.containsKey(anno.gameId())){
				controllerMap.put(anno.gameId(), new HashMap<String, Object>());
			}
			controllerMap.get(anno.gameId()).put(entry.getKey(), entry.getValue());
			initAndCheckMethodsByNettyRpcCall(anno.gameId(), entry.getValue(), entry.getKey());
		}
	}

	public void initAndCheckMethodsByNettyRpcCall(long gameId, Object controller, String controllerName) {
		if(!methodMap.containsKey(gameId)){
			methodMap.put(gameId, new HashMap<String, Method>());
		}
		if(!nettyRpcCallAnnoValueMap.containsKey(gameId)){
			nettyRpcCallAnnoValueMap.put(gameId, new HashMap<String, String>());
		}
		if(!methodParamIndexMap.containsKey(gameId)){
			methodParamIndexMap.put(gameId, new HashMap<String, Map<Integer, String>>());
		}
		Class<?> currentClazz = controller.getClass();
		WsRpcController nettyRpcAnno = controller.getClass().getAnnotation(WsRpcController.class);
		while (currentClazz != Object.class) {
			Method[] methods = currentClazz.getDeclaredMethods();
			for (final Method method : methods) {
				if (method.getModifiers() == Modifier.PUBLIC && method.isAnnotationPresent(WsRpcCall.class)) {
					WsRpcCall anno = method.getAnnotation(WsRpcCall.class);
					if (Strings.isNullOrEmpty(anno.value())) {
						throw new NettyRpcCallException("NettyRpcCall value can't be empty");
					}
					String requestPath = NettyHttpUtil
							.resolveUrlPath(NettyHttpUtil.resolveStartWithEscape(nettyRpcAnno.value()))
							+ NettyHttpUtil.resolveStartWithEscape(anno.value());

					if (methodMap.get(gameId).containsKey(requestPath)) {
						throw new NettyRpcCallException("NettyRpcCall value must be unique");
					}
					try {
						methodParamIndexMap.get(gameId).put(requestPath, MethodUtil.getParameterIndexMap(method));
					} catch (NotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					methodMap.get(gameId).put(requestPath, method);
					nettyRpcCallAnnoValueMap.get(gameId).put(requestPath, controllerName);
				}
			}
			currentClazz = currentClazz.getSuperclass();
		}
	}

	private Map<Integer, String> getParamsIndex(long gameId, String requestUri) {
		return methodParamIndexMap.get(gameId).get(requestUri);
	}

	public static void main(String[] args) {
		WsRpcCallInvoker manager = new WsRpcCallInvoker();
		manager.init();
	}
}
