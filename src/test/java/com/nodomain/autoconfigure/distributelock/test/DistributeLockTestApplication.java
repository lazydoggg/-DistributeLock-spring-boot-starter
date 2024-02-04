package com.nodomain.autoconfigure.distributelock.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author: zph
 * @date: 2024/02/04
 * @description: 测试启动类
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class DistributeLockTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributeLockTestApplication.class,args);
    }

}
