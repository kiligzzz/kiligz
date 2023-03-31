package com.kiligz.trace.listener;

import org.slf4j.TtlMDCAdapter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 替换MDC的监听器
 *
 * @author Ivan
 * @since 2023/3/31
 */
@Component
public class ReplaceMDCListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        TtlMDCAdapter.replace();
    }
}
