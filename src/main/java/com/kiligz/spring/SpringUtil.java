package com.kiligz.spring;

import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring工具类
 *
 * @author Ivan
 * @date 2022/9/8 16:27
 */
@Component
public class SpringUtil implements ApplicationContextAware {
    private static ApplicationContext context;

    private SpringUtil() {
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public static Object getBean(String name) {
        return context.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }

    public static <T> List<T> getBeans(Class<T> clazz) {
        Map<String, T> beanMap = context.getBeansOfType(clazz);
        return new ArrayList<>(beanMap.values());
    }
}


