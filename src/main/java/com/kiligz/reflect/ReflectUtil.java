package com.kiligz.reflect;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * 反射工具类，高速缓存ASM
 * 只能在权限内访问方法或属性
 *
 * @author Ivan
 * @since 2022/11/28
 */
public class ReflectUtil {
    /**
     * 方法访问器缓存
     */
    private static final ConcurrentMap<Class<?>, MethodAccess> METHOD_ACCESS_MAP = Maps.newConcurrentMap();

    /**
     * 属性访问器缓存
     */
    private static final ConcurrentMap<Class<?>, FieldAccess> FIELD_ACCESS_MAP = Maps.newConcurrentMap();

    /**
     * 获取方法访问器
     */
    public static MethodAccess getMethodAccess(Class<?> clazz) {
        return getAccess(METHOD_ACCESS_MAP, clazz, MethodAccess::get);
    }

    /**
     * 获取属性访问器
     */
    public static FieldAccess getFieldAccess(Class<?> clazz) {
        return getAccess(FIELD_ACCESS_MAP, clazz, FieldAccess::get);
    }

    /**
     * 获取访问器
     */
    private static <T> T getAccess(ConcurrentMap<Class<?>, T> accessMap, Class<?> clazz,
                                   Function<Class<?>, T> accessFunction) {
        return accessMap.computeIfAbsent(clazz, accessFunction);
    }
}
