package com.netease.pangu.game.util;

import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
        while (!Modifier.isStatic(cm.getModifiers()) && !StringUtils.equals(attr.variableName(pos++), "this")) {
        }
        Map<Integer, String> paramIndexMap = new HashMap<Integer, String>();
        for (int i = 0; i < cm.getParameterTypes().length; i++) {
            paramIndexMap.put(i, attr.variableName(i + pos));
        }
        return paramIndexMap;
    }
}
