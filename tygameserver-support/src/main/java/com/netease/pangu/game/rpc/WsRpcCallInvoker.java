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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.rpc.annotation.WsRpcCall;
import com.netease.pangu.game.rpc.annotation.WsRpcController;
import com.netease.pangu.game.util.NettyHttpUtil;

@Component
public class WsRpcCallInvoker {
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

	private Method getMethod(String rpcMethodName) {
		return methodMap.get(rpcMethodName);
	}

	private Object getController(String rpcMethodName) {
		return controllerMap.get(nettyRpcCallAnnoValueMap.get(rpcMethodName));
	}
	
	public Object invoke(String rpcMethodName, List<Object> args, GameContext context) {
		Method method = getMethod(rpcMethodName);
		Object controller = getController(rpcMethodName);
		Class<?>[] paramTypes = method.getParameterTypes();
		List<Object> convertedArgs = new ArrayList<Object>();
		for (int i = 0; i < paramTypes.length; i++) {
			if (Long.class.isAssignableFrom(paramTypes[i]) || long.class.isAssignableFrom(paramTypes[i])) {
				Double num = NumberUtils.toDouble(String.valueOf(args.get(i)));
				convertedArgs.add(num.longValue());
			} else if (Integer.class.isAssignableFrom(paramTypes[i]) || int.class.isAssignableFrom(paramTypes[i])) {
				Double num = NumberUtils.toDouble(String.valueOf(args.get(i)));
				convertedArgs.add(num.intValue());
			} else if (Double.class.isAssignableFrom(paramTypes[i]) || double.class.isAssignableFrom(paramTypes[i])) {
				Double num = NumberUtils.toDouble(String.valueOf(args.get(i)));
				convertedArgs.add(num);
			} else if (Float.class.isAssignableFrom(paramTypes[i]) || float.class.isAssignableFrom(paramTypes[i])) {
				Double num = NumberUtils.toDouble(String.valueOf(args.get(i)));
				convertedArgs.add(num.floatValue());
			} else if (String.class.isAssignableFrom(paramTypes[i])) {
				convertedArgs.add(String.valueOf(args.get(i)));
			} else if (GameContext.class.isAssignableFrom(paramTypes[i])) {
				convertedArgs.add(context);
			}
		}
		try {
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
		return null;
	}

	@PostConstruct
	public void init() {
		methodMap = new HashMap<String, Method>();
		nettyRpcCallAnnoValueMap = new HashMap<String, String>();
		controllerMap = beanFactory.getBeansWithAnnotation(WsRpcController.class);
		for (Entry<String, Object> entry : controllerMap.entrySet()) {
			initAndCheckMethodsByNettyRpcCall(entry.getKey(), entry.getValue().getClass());
		}
	}

	public void initAndCheckMethodsByNettyRpcCall(String controllerName, Class<?> clazz) {
		Class<?> currentClazz = clazz;
		Object controller = controllerMap.get(controllerName);
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

					if (methodMap.containsKey(requestPath)) {
						throw new NettyRpcCallException("NettyRpcCall value must be unique");
					}
					methodMap.put(requestPath, method);
					nettyRpcCallAnnoValueMap.put(requestPath, controllerName);
				}
			}
			currentClazz = currentClazz.getSuperclass();
		}
	}

	public static void main(String[] args) {
		WsRpcCallInvoker manager = new WsRpcCallInvoker();
		manager.init();
	}
}
