package com.kiligz.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiligz.spring.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nacos工具类（用于实时获取nacos上的配置，并转换为相应格式）
 * <pre>
 * - 懒汉式单例获取配置，第一次调用时初始化、获取、格式化，不会重复
 * - 监听nacos上的配置，发生变化实时更新
 * - 若更新时出错，则放弃本次更新，使用上次正常更新时的配置
 * </pre>
 *
 * @author Ivan
 * @since 2022/11/25
 */
@Slf4j
@Component
@SuppressWarnings("all")
public class NacosConfigUtil {
    /**
     * dataId枚举 -> obj的map
     */
    private static final Map<DataId, Object> dataIdToValueMap = new ConcurrentHashMap<>();

    @Value("${spring.cloud.nacos.server-addr}")
    private static String serverAddr;

    @Value("${spring.cloud.nacos.config.group}")
    private static String group;

    /**
     * 获取nacos配置可直接访问的url
     */
    public static String[] getDirectUrls() {
        return (String[]) get(DataId.DIRECT_URL);
    }

    /**
     * 获取dataId对应的对象
     */
    private static Object get(DataId dataId) {
        return SpringUtil.getBean(NacosConfigUtil.class).lazyGet(dataId);
    }

    /**
     * 从dataIdToValueMap中获取值（懒汉式单例获取）
     */
    private Object lazyGet(DataId dataId) {
        // 保证只放入一次 且 只添加一次监听器
        dataIdToValueMap.computeIfAbsent(dataId, key -> {
            try {
                // 获取配置
                String content = getConfigService().getConfig(dataId.dataId, group, 5000);
                // 添加监听器，若发生改变则刷新
                getConfigService().addListener(dataId.dataId, group, new NacosListener(dataId));
                return key.convert(content);
            } catch (Exception e) {
                throw new RuntimeException();
            }
        });
        return dataIdToValueMap.get(dataId);
    }


    /**
     * nacos监听器
     */
    @AllArgsConstructor
    private static class NacosListener extends AbstractListener {
        private final DataId dataId;

        @Override
        public void receiveConfigInfo(String content) {
            try {
                dataIdToValueMap.put(dataId, dataId.convert(content));
                log.info("更新nacos配置成功 {}.", dataId.dataId);
            } catch (Exception e) {
                log.error("更新nacos配置出错，将使用上次正常的配置.", e);
            }
        }
    }

    /**
     * 单例获取ConfigService
     */
    public static ConfigService getConfigService() {
        return ConfigServiceHolder.instance;
    }

    /**
     * 静态内部类持有ConfigService
     */
    private static class ConfigServiceHolder {
        private static final ConfigService instance;
        
        static {
            try {
                instance = NacosFactory.createConfigService(serverAddr);
            } catch (NacosException e) {
                throw new RuntimeException();
            }
        }
    }

    /**
     * dataId的枚举
     */
    @AllArgsConstructor
    private enum DataId {
        DIRECT_URL("direct_url.json", new TypeReference<String[]>() {});

        /**
         * 文件名
         */
        String dataId;

        /**
         * 转换的类型引用
         */
        TypeReference typeReference;

        static ObjectMapper mapper;

        /**
         * 转换为指定类型
         */
        public Object convert(String content) throws JsonProcessingException {
            return mapper.readValue(content, typeReference);
        }
    }
}
