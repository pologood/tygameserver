package com.netease.pangu.game.http;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
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
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;
import com.netease.pangu.game.util.JsonUtil;
import com.netease.pangu.game.util.MethodUtil;
import com.netease.pangu.game.util.NettyHttpUtil;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import javassist.NotFoundException;

@Component
public class HttpRequestInvoker {
	private final static Logger logger = Logger.getLogger(HttpRequestInvoker.class);
	private Map<String, Object> controllerMap;
	private Map<String, Method> methodMap;
	private Map<String, Map<Integer, String>> methodParamIndexMap;
	private Map<String, String> httpRequestMappingAnnoValueMap;
	@Resource
	private ConfigurableListableBeanFactory beanFactory;

	public static class HttpRequestException extends RuntimeException {
		private static final long serialVersionUID = -169844953289757522L;

		public HttpRequestException(String msg) {
			super(msg);
		}

		public HttpRequestException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	private Method getMethod(String requestUri) {
		return methodMap.get(requestUri);
	}

	private Map<Integer, String> getParamsIndex(String requestUri) {
		return methodParamIndexMap.get(requestUri);
	}

	public boolean containsURIPath(String requestUri) {
		return methodMap.keySet().contains(requestUri);
	}

	private Object getController(String requestUri) {
		return controllerMap.get(httpRequestMappingAnnoValueMap.get(requestUri));

	}

	/***
	 * 需要处理默认参数和别名的逻辑
	 * 
	 * @param requestUri
	 * @param args
	 * @param request
	 * @return
	 */
	public <Player> FullHttpResponse invoke(String requestUri, Map<String, String> args, FullHttpRequest request) {
		Method method = getMethod(requestUri);
		Object controller = getController(requestUri);
		final Parameter[] parameters = method.getParameters();
		try {
			Map<Integer, String> paramsIndex = getParamsIndex(requestUri);
			List<Object> convertedArgs = new ArrayList<Object>();
			for (Integer i = 0; i < parameters.length; i++) {
				if (Long.class.isAssignableFrom(parameters[i].getType())
						|| long.class.isAssignableFrom(parameters[i].getType())) {
					String arg = args.get(paramsIndex.get(i));
					if (arg == null) {
						logger.info(String.format("parameter %s is null", arg));
						return NettyHttpUtil.createBadRequestResponse();
					}
					Double num = NumberUtils.toDouble(arg);
					convertedArgs.add(num.longValue());
				} else if (Integer.class.isAssignableFrom(parameters[i].getType())
						|| int.class.isAssignableFrom(parameters[i].getType())) {
					String arg = args.get(paramsIndex.get(i));
					if (arg == null) {
						logger.info(String.format("parameter %s is null", arg));
						return NettyHttpUtil.createBadRequestResponse();
					}
					Double num = NumberUtils.toDouble(arg);
					convertedArgs.add(num.intValue());
				} else if (Double.class.isAssignableFrom(parameters[i].getType())
						|| double.class.isAssignableFrom(parameters[i].getType())) {
					String arg = args.get(paramsIndex.get(i));
					if (arg == null) {
						logger.info(String.format("parameter %s is null", arg));
						return NettyHttpUtil.createBadRequestResponse();
					}
					Double num = NumberUtils.toDouble(arg);
					convertedArgs.add(num);
				} else if (Float.class.isAssignableFrom(parameters[i].getType())
						|| float.class.isAssignableFrom(parameters[i].getType())) {
					String arg = args.get(paramsIndex.get(i));
					if (arg == null) {
						logger.info(String.format("parameter %s is null", arg));
						return NettyHttpUtil.createBadRequestResponse();
					}
					Double num = NumberUtils.toDouble(arg);
					convertedArgs.add(num.floatValue());
				} else if (String.class.isAssignableFrom(parameters[i].getType())) {
					String arg = args.get(paramsIndex.get(i));
					if (arg == null) {
						logger.info(String.format("parameter %s is null", arg));
						return NettyHttpUtil.createBadRequestResponse();
					}
					convertedArgs.add(arg);
				} else if (HttpRequest.class.isAssignableFrom(parameters[i].getType())) {
					convertedArgs.add(request);
				}
			}
			Object result = method.invoke(controller, convertedArgs.toArray(new Object[0]));
			return NettyHttpUtil.createHttpResponse(HttpResponseStatus.OK, JsonUtil.toJson(result));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return NettyHttpUtil.createBadRequestResponse();
	}

	@PostConstruct
	public void init() {
		methodMap = new HashMap<String, Method>();
		httpRequestMappingAnnoValueMap = new HashMap<String, String>();
		methodParamIndexMap = new HashMap<String, Map<Integer, String>>();
		controllerMap = beanFactory.getBeansWithAnnotation(HttpController.class);
		for (Entry<String, Object> entry : controllerMap.entrySet()) {
			initAndCheckMethodsByHttpRequest(entry.getKey(), entry.getValue().getClass());
		}
	}

	public void initAndCheckMethodsByHttpRequest(String controllerName, Class<?> clazz) {
		Class<?> currentClazz = clazz;
		Object controller = controllerMap.get(controllerName);
		HttpController httpAnno = controller.getClass().getAnnotation(HttpController.class);
		while (currentClazz != Object.class) {
			Method[] methods = currentClazz.getDeclaredMethods();
			for (final Method method : methods) {
				if (method.getModifiers() == Modifier.PUBLIC && method.isAnnotationPresent(HttpRequestMapping.class)) {
					HttpRequestMapping anno = method.getAnnotation(HttpRequestMapping.class);
					if (Strings.isNullOrEmpty(anno.value())) {
						throw new HttpRequestException("HttpRequestMapping value can't be empty");
					}
					String requestPath = NettyHttpUtil
							.resolveUrlPath(NettyHttpUtil.resolveStartWithEscape(httpAnno.value()))
							+ NettyHttpUtil.resolveStartWithEscape(anno.value());
					if (methodMap.containsKey(requestPath)) {
						throw new HttpRequestException("HttpRequestMapping value must be unique");
					}
					methodMap.put(requestPath, method);
					try {
						methodParamIndexMap.put(requestPath, MethodUtil.getParameterIndexMap(method));
					} catch (NotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					httpRequestMappingAnnoValueMap.put(requestPath, controllerName);
				}
			}
			currentClazz = currentClazz.getSuperclass();
		}
	}
}
