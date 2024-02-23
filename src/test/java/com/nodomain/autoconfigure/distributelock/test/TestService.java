package com.nodomain.autoconfigure.distributelock.test;

import com.nodomain.autoconfigure.distributelock.annotation.DistributeLock;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author: zph
 * @date: 2024/02/23
 * @description:
 */
@Service
public class TestService {

    @DistributeLock(keys = {"#userId"})
    public String testLock(String userId) throws InterruptedException {
        System.out.println("拿到锁..." + Thread.currentThread().getName() + "..." + LocalDateTime.now());
        Thread.sleep(3 * 1000);
        return "success";
    }
}
