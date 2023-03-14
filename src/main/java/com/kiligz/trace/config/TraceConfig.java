package com.kiligz.trace.config;

import com.kiligz.trace.domain.Trace;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 添加http拦截器，添加rpc过滤器
 *
 * @author Ivan
 * @since 2023/3/10
 */
@Configuration
public class TraceConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TraceInterceptor())
                .addPathPatterns("/**");
    }

//    @Bean
//    public Filter traceFilter() {
//        return new TraceFilter();
//    }

    /**
     * 全链路追踪的http请求拦截器
     */
    public static class TraceInterceptor implements HandlerInterceptor {
        /**
         * 初始化Trace存入TransmittableThreadLocal、MDC中
         */
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            Trace.init();
            return true;
        }

        /**
         * 清理TransmittableThreadLocal、MDC中的值
         */
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            Trace.remove();
        }
    }

    /**
     * 全链路追踪的过滤器，rpc请求处理，以Dubbo3为例（暂时不导入）
     */
//    @Activate(group = "${groupName}")
//    public class TraceFilter implements Filter {
//        @Override
//        public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
//            Result result;
//            try {
//                // 更新Trace
//                for (Object arg : invocation.getArguments()) {
//                    if (arg instanceof Trace) {
//                        Trace.refresh((Trace) arg);
//                        break;
//                    }
//                }
//                result = invoker.invoke(invocation);
//            } finally {
//                // 清空删除ThreadLocal
//                Trace.remove();
//            }
//            return result;
//        }
//    }
}
