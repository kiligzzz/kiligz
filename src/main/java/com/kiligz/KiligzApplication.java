package com.kiligz;

import org.slf4j.TtlMDCAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KiligzApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(KiligzApplication.class);
        // 添加初始化器替换MDC的mdcAdapter
        springApplication.addInitializers(
                applicationContext -> TtlMDCAdapter.replace());
        springApplication.run(args);
//        SpringApplication.run(KiligzApplication.class, args);
    }
}
