package com.kiligz.reflectAsm;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.collect.Maps;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 使用高速缓存ASM实现的beanCopy
 *
 * @author Ivan
 * @since 2022/5/14
 */
public class BeanUtil {
    /**
     * 方法访问器缓存
     */
    private static final ConcurrentMap<Class<?>, MethodAccess> cache = Maps.newConcurrentMap();

    /**
     * 获取方法访问器
     */
    public static MethodAccess getMethodAccess(Class<?> clazz) {
        if(cache.containsKey(clazz)) {
            return cache.get(clazz);
        }
        MethodAccess methodAccess = MethodAccess.get(clazz);
        cache.putIfAbsent(clazz, methodAccess);
        return methodAccess;
    }

    /**
     * 复制属性并返回to对象
     */
    public static <F, T> T copy(F from, T to) {
        Class<?> fromClass = from.getClass();
        Class<?> toClass = to.getClass();
        MethodAccess fromMethodAccess = getMethodAccess(fromClass);
        MethodAccess toMethodAccess = getMethodAccess(toClass);

        Field[] fromFields = fromClass.getDeclaredFields();
        List<String> toNames = getFieldNames(toClass);

        for(Field field : fromFields) {
            String name = field.getName();
            if (toNames.contains(name)) {
                String capitalize = StringUtils.capitalize(name);
                String fromPrefix = field.getType() == Boolean.class ? "is" : "get";

                Object value = fromMethodAccess.invoke(from, fromPrefix + capitalize);
                toMethodAccess.invoke(to, "set" + capitalize, value);
            }
        }
        return to;
    }

    /**
     * 获取所有属性名
     */
    private static List<String> getFieldNames(Class<?> toClass) {
        return Arrays.stream(toClass.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());
    }
}