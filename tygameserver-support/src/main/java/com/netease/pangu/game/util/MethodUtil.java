package com.netease.pangu.game.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class MethodUtil {
	public static Map<Integer, String> getParameterIndexMap(Method method) throws NotFoundException {
		ClassPool pool = ClassPool.getDefault();
		Class<?> clazz = method.getDeclaringClass();
		CtClass clz = pool.getCtClass(clazz.getName());
		CtClass[] params = new CtClass[method.getParameterTypes().length];
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			params[i] = pool.getCtClass(method.getParameterTypes()[i].getName());
		}
		CtMethod cm = clz.getDeclaredMethod(method.getName(), params);
		MethodInfo methodInfo = cm.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
		int pos = 0;
		while(!StringUtils.equals(attr.variableName(pos++), "this")){			
		}
		Map<Integer, String> paramIndexMap = new HashMap<Integer, String>();
		for (int i = 0; i < cm.getParameterTypes().length; i++) {
			paramIndexMap.put(i, attr.variableName(i+pos));
		}
		return paramIndexMap;
	}
}
