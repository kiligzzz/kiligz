package com.kiligz.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 方法工具类
 * 可执行指定类或实例的任意方法
 *
 * @author Ivan
 * @since 2023/4/26
 */
@SuppressWarnings("all")
public class MethodUtil {

    /**
     * 执行指定实例的任意方法
     */
    public static <T> T invoke(Object caller, String methodName, Object... args) {
        return (T) invoke(caller, caller.getClass(), methodName, args);
    }

    /**
     * 执行指定类的任意方法
     */
    public static <T> T invokeStatic(Class<?> caller, String methodName, Object... args) {
        return (T) invoke(null, caller, methodName, args);
    }

    /**
     * 执行逻辑
     */
    private static Object invoke(Object obj, Class<?> clazz, String methodName, Object... args)  {
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }
        try {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)
                        && matchParamTypes(method.getParameterTypes(), paramTypes)) {
                    method.setAccessible(true);
                    return method.invoke(obj, args);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Method invoke error", e);
        }
        throw new RuntimeException("No such method " + methodName + "(" + Arrays.toString(paramTypes) + ")");
    }

    /**
     * 匹配参数列表类型
     */
    private static boolean matchParamTypes(Class<?>[] expectedTypes, Class<?>[] actualTypes) {
        if (expectedTypes.length != actualTypes.length) {
            return false;
        }
        for (int i = 0; i < expectedTypes.length; i++) {
            if (expectedTypes[i] == null || actualTypes[i] == null) {
                if (expectedTypes[i] != actualTypes[i]) {
                    return false;
                }
            } else if (!expectedTypes[i].isAssignableFrom(actualTypes[i])
                    && !isAutoBoxingMatch(expectedTypes[i], actualTypes[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 自动装箱匹配
     */
    private static boolean isAutoBoxingMatch(Class<?> expectedType, Class<?> actualType) {
        if (expectedType.isPrimitive()) {
            return toWrapperClass(expectedType) == actualType;
        } else if (actualType.isPrimitive()) {
            return toWrapperClass(actualType) == expectedType;
        } else {
            return false;
        }
    }

    /**
     * 转为装箱后的Class
     */
    private static Class<?> toWrapperClass(Class<?> type) {
        if (type == int.class) {
            return Integer.class;
        } else if (type == boolean.class) {
            return Boolean.class;
        } else if (type == long.class) {
            return Long.class;
        } else if (type == double.class) {
            return Double.class;
        } else if (type == float.class) {
            return Float.class;
        } else if (type == char.class) {
            return Character.class;
        } else if (type == byte.class) {
            return Byte.class;
        } else if (type == short.class) {
            return Short.class;
        } else {
            return type;
        }
    }

}
