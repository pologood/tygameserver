package com.netease.pangu.game.http;

import com.google.common.base.Strings;
import com.netease.pangu.game.http.annotation.Anonymous;
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
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Component
public class HttpRequestInvoker {
    private final static Logger logger = Logger.getLogger(HttpRequestInvoker.class);
    private Map<Long, Map<String, Object>> controllerMap;
    private Map<Long, Map<String, Method>> methodMap;
    private Map<Long, Map<String, Map<Integer, String>>> methodParamIndexMap;
    private Map<Long, Map<String, String>> httpRequestMappingAnnoValueMap;
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

    private Method getMethod(long gameId, String requestUri) {
        return methodMap.get(gameId).get(requestUri);
    }

    public boolean isNeedAuth(long gameId, String requestUri){
        Method method = getMethod(gameId, requestUri);
        if(method != null){
            Anonymous anno = method.getDeclaredAnnotation(Anonymous.class);
            if(anno != null){
                return false;
            }
        }
        return true;
    }

    private Map<Integer, String> getParamsIndex(long gameId, String requestUri) {
        return methodMap.get(gameId) != null ? methodParamIndexMap.get(gameId).get(requestUri): null;
    }

    public boolean containsURIPath(long gameId, String requestUri) {
        return methodMap.get(gameId) != null ? methodMap.get(gameId).keySet().contains(requestUri):false;
    }

    private Object getController(long gameId, String requestUri) {
        return methodMap.get(gameId) != null ?  controllerMap.get(gameId).get(httpRequestMappingAnnoValueMap.get(gameId).get(requestUri)): null;

    }

    /***
     * 需要处理默认参数和别名的逻辑
     *
     * @param requestUri
     * @param args
     * @param request
     * @return
     */
    public <Player> void invoke(long gameId, String requestUri, Map<String, String> args, FullHttpRequest request, FullHttpResponse response) {
        Method method = getMethod(gameId, requestUri);
        Object controller = getController(gameId, requestUri);
        final Class<?>[] parameterTypes = method.getParameterTypes();
        try {
            logger.info(JsonUtil.toJson(args));
            Map<Integer, String> paramsIndex = getParamsIndex(gameId, requestUri);
            logger.info(JsonUtil.toJson(paramsIndex));
            List<Object> convertedArgs = new ArrayList<Object>();
            for (Integer i = 0; i < parameterTypes.length; i++) {
                if (Long.class.isAssignableFrom(parameterTypes[i])
                        || long.class.isAssignableFrom(parameterTypes[i])) {
                    String arg = args.get(paramsIndex.get(i));
                    if (arg == null) {
                        logger.info(String.format("parameter %s is null", paramsIndex.get(i)));
                        NettyHttpUtil.setHttpResponse(response, HttpResponseStatus.BAD_REQUEST);
                        return;
                    }
                    Double num = NumberUtils.toDouble(arg);
                    convertedArgs.add(num.longValue());
                } else if (Integer.class.isAssignableFrom(parameterTypes[i])
                        || int.class.isAssignableFrom(parameterTypes[i])) {
                    String arg = args.get(paramsIndex.get(i));
                    if (arg == null) {
                        logger.info(String.format("parameter %s is null", paramsIndex.get(i)));
                        NettyHttpUtil.setHttpResponse(response, HttpResponseStatus.BAD_REQUEST);
                        return;
                    }
                    Double num = NumberUtils.toDouble(arg);
                    convertedArgs.add(num.intValue());
                } else if (Double.class.isAssignableFrom(parameterTypes[i])
                        || double.class.isAssignableFrom(parameterTypes[i])) {
                    String arg = args.get(paramsIndex.get(i));
                    if (arg == null) {
                        logger.info(String.format("parameter %s is null", paramsIndex.get(i)));
                        NettyHttpUtil.setHttpResponse(response, HttpResponseStatus.BAD_REQUEST);
                        return;
                    }
                    Double num = NumberUtils.toDouble(arg);
                    convertedArgs.add(num);
                } else if (Float.class.isAssignableFrom(parameterTypes[i])
                        || float.class.isAssignableFrom(parameterTypes[i])) {
                    String arg = args.get(paramsIndex.get(i));
                    if (arg == null) {
                        logger.info(String.format("parameter %s is null", paramsIndex.get(i)));
                        NettyHttpUtil.setHttpResponse(response, HttpResponseStatus.BAD_REQUEST);
                        return;
                    }
                    Double num = NumberUtils.toDouble(arg);
                    convertedArgs.add(num.floatValue());
                } else if (String.class.isAssignableFrom(parameterTypes[i])) {
                    String arg = args.get(paramsIndex.get(i));
                    if (arg == null) {
                        logger.info(String.format("parameter %s is null", paramsIndex.get(i)));
                        NettyHttpUtil.setHttpResponse(response, HttpResponseStatus.BAD_REQUEST);
                        return;
                    }
                    convertedArgs.add(arg);
                } else if (HttpRequest.class.isAssignableFrom(parameterTypes[i])) {
                    convertedArgs.add(request);
                }
            }
            Object result = method.invoke(controller, convertedArgs.toArray(new Object[0]));
            if (String.class.isAssignableFrom(result.getClass())) {
                NettyHttpUtil.setHttpResponse(response, HttpResponseStatus.OK, (String) result);
                return;
            } else {
                NettyHttpUtil.setHttpResponse(response, HttpResponseStatus.OK, JsonUtil.toJson(result));
                return;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        NettyHttpUtil.setHttpResponse(response, HttpResponseStatus.BAD_REQUEST);
        return;
    }

    @PostConstruct
    public void init() {
        methodMap = new HashMap<Long, Map<String, Method>>();
        httpRequestMappingAnnoValueMap = new HashMap<Long, Map<String, String>>();
        methodParamIndexMap = new HashMap<Long, Map<String, Map<Integer, String>>>();
        controllerMap = new HashMap<Long, Map<String, Object>>();
        Map<String, Object> controllers = beanFactory.getBeansWithAnnotation(HttpController.class);
        for (Entry<String, Object> entry : controllers.entrySet()) {
            HttpController anno = entry.getValue().getClass().getAnnotation(HttpController.class);
            if (!controllerMap.containsKey(anno.gameId())) {
                controllerMap.put(anno.gameId(), new HashMap<String, Object>());
            }
            controllerMap.get(anno.gameId()).put(entry.getKey(), entry.getValue());
            initAndCheckMethodsByHttpRequest(anno.gameId(), entry.getValue(), entry.getKey());
        }
    }

    public void initAndCheckMethodsByHttpRequest(long gameId, Object controller, String controllerName) {
        if (!methodMap.containsKey(gameId)) {
            methodMap.put(gameId, new HashMap<String, Method>());
        }
        if (!httpRequestMappingAnnoValueMap.containsKey(gameId)) {
            httpRequestMappingAnnoValueMap.put(gameId, new HashMap<String, String>());
        }
        if (!methodParamIndexMap.containsKey(gameId)) {
            methodParamIndexMap.put(gameId, new HashMap<String, Map<Integer, String>>());
        }
        Class<?> currentClazz = controller.getClass();
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
                    if (methodMap.get(gameId).containsKey(requestPath)) {
                        throw new HttpRequestException("HttpRequestMapping value must be unique");
                    }
                    methodMap.get(gameId).put(requestPath, method);
                    try {
                        methodParamIndexMap.get(gameId).put(requestPath, MethodUtil.getParameterIndexMap(method));
                    } catch (NotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    httpRequestMappingAnnoValueMap.get(gameId).put(requestPath, controllerName);
                }
            }
            currentClazz = currentClazz.getSuperclass();
        }
    }
}
