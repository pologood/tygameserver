package com.netease.pangu.game.http;

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
import org.omg.CosNaming.NamingContextExtPackage.URLStringHelper;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.google.common.base.Strings;
import com.google.common.net.UrlEscapers;
import com.netease.pangu.game.common.meta.GameContext;
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;
import com.netease.pangu.game.util.NettyHttpUtil;

public class HttpRequestInvoker {
	private Map<String, Object> controllerMap;
	private Map<String, Method> methodMap;
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
	
	private Method getMethod(String rpcMethodName){
		return methodMap.get(rpcMethodName);
	}
	
	private Object getController(String rpcMethodName){
		return controllerMap.get(httpRequestMappingAnnoValueMap.get(rpcMethodName));
		
	}
	
	public Object invoke(String rpcMethodName, List<Object> args, GameContext context){
		Method method = getMethod(rpcMethodName);
		Object controller = getController(rpcMethodName);
		Class<?>[] paramTypes = method.getParameterTypes();
		List<Object> convertedArgs = new ArrayList<Object>();
		for(int i = 0; i < paramTypes.length; i++){
			if(Long.class.isAssignableFrom(paramTypes[i])|| long.class.isAssignableFrom(paramTypes[i])){
				Double num = NumberUtils.toDouble(String.valueOf(args.get(i)));
				convertedArgs.add(num.longValue());
			}else if(Integer.class.isAssignableFrom(paramTypes[i])|| int.class.isAssignableFrom(paramTypes[i])){
				Double num = NumberUtils.toDouble(String.valueOf(args.get(i)));
				convertedArgs.add(num.intValue());
			}else if(Double.class.isAssignableFrom(paramTypes[i])|| double.class.isAssignableFrom(paramTypes[i])){
				Double num = NumberUtils.toDouble(String.valueOf(args.get(i)));
				convertedArgs.add(num);
			}else if(Float.class.isAssignableFrom(paramTypes[i])|| float.class.isAssignableFrom(paramTypes[i])){
				Double num = NumberUtils.toDouble(String.valueOf(args.get(i)));
				convertedArgs.add(num.floatValue());
			}else if(String.class.isAssignableFrom(paramTypes[i])){
				convertedArgs.add(String.valueOf(args.get(i)));
			}else if(GameContext.class.isAssignableFrom(paramTypes[i])){
				convertedArgs.add(context);
			}
		}
		try {
			return method.invoke(controller, convertedArgs.toArray(new Object[0]));
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
		httpRequestMappingAnnoValueMap = new HashMap<String, String>();
		controllerMap = beanFactory.getBeansWithAnnotation(HttpController.class);
		for (Entry<String, Object> entry : controllerMap.entrySet()) {
			initAndCheckMethodsByHttpRequest(entry.getKey(), entry.getValue().getClass());
		}
	}

	public void initAndCheckMethodsByHttpRequest(String controllerName, Class<?> clazz){
		Class<?> currentClazz = clazz;
		Object controller = controllerMap.get(controllerName);
		HttpController httpAnno = controller.getClass().getAnnotation(HttpController.class);	
		while(currentClazz != Object.class){
			Method[] methods = currentClazz.getDeclaredMethods();
			for(final Method method: methods){
				if(method.getModifiers() == Modifier.PUBLIC && method.isAnnotationPresent(HttpRequestMapping.class)){
					HttpRequestMapping anno = method.getAnnotation(HttpRequestMapping.class);
					if(Strings.isNullOrEmpty(anno.value())){
						throw new HttpRequestException("HttpRequestMapping value can't be empty");
					}
					String requestPath = NettyHttpUtil.resolveUrlPath(NettyHttpUtil.resolveStartWithEscape(httpAnno.value())) + NettyHttpUtil.resolveStartWithEscape(anno.value());
					if(methodMap.containsKey(requestPath)){
						throw new HttpRequestException("HttpRequestMapping value must be unique");
					}
					methodMap.put(requestPath, method);
					httpRequestMappingAnnoValueMap.put(requestPath, controllerName);
				}
			}
			currentClazz = currentClazz.getSuperclass();
		}
	}
}
