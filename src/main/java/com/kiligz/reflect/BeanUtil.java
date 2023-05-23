package com.kiligz.reflect;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * 使用高速缓存ASM实现的beanCopy
 *
 * @author Ivan
 * @since 2022/5/14
 */
public class BeanUtil {
    /**
     * 复制属性并返回to对象
     */
    public static <F, T> T copy(F from, T to) {
        Class<?> fromClass = from.getClass();
        Class<?> toClass = to.getClass();
        MethodAccess fromMethodAccess = ReflectUtil.getMethodAccess(fromClass);
        MethodAccess toMethodAccess = ReflectUtil.getMethodAccess(toClass);

        FieldAccess fromFieldAccess = ReflectUtil.getFieldAccess(fromClass);
        List<String> toNames = Arrays.asList(fromFieldAccess.getFieldNames());

        for(Field field : fromFieldAccess.getFields()) {
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
}