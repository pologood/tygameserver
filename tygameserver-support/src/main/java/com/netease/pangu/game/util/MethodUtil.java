package com.netease.pangu.game.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class MethodUtil {
	private static final ClassPool pool = new ClassPool(true);

	public static Map<Integer, String> getParameterIndexMap(Method method) throws NotFoundException {
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
		Map<Integer, String> paramIndexMap = new HashMap<Integer, String>();
		for (int i = 0; i < cm.getParameterTypes().length + 1; i++) {
			String name = attr.variableName(i);
			if(!name.equals("this")){
				paramIndexMap.put(i, name);
			}
		}
		return paramIndexMap;
	}
}
