package com.kiligz.trace;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.kiligz.concurrent.Concurrents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;

/**
 * 分布式异步全链路追踪
 * TransmittableThreadLocal、Trace对象、拦截器、过滤器、重写MDCAdapter等实现
 *
 * @author Ivan
 * @since 2023/3/10
 */
@Slf4j
@RestController
public class TraceController {
    private static final Executor traceExecutor =
            // 先拷贝到任务线程，执行完后再将值拷贝回去（CopyOnWrite思想）
            TtlExecutors.getTtlExecutor(
                    Concurrents.getFixedThreadPool("trace"));

    @GetMapping("trace")
    public void trace() {
        log.info("main......");
        traceExecutor.execute(() -> log.info("threadPool......."));
    }
}
