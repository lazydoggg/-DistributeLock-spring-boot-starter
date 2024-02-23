package com.nodomain.autoconfigure.distributelock.test;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author: zph
 * @date: 2024/02/04
 * @description: 测试Redisson配置
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DistributeLockTestApplication.class)
public class RedissonTest {


    @Autowired
    TestService testService;



    @Test
    public void multithreadingTestLock() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        for (int i = 0; i < 5; i++) {
            executorService.execute(()->{
                try {
                    testService.testLock("111");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            });

        }
        executorService.awaitTermination(30, TimeUnit.DAYS);//防止主线程销毁导致spring容器销毁

    }

}
