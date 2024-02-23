package com.nodomain.autoconfigure.distributelock.handler.lock;

import com.nodomain.autoconfigure.distributelock.lock.Lock;
import com.nodomain.autoconfigure.distributelock.model.LockInfo;
import org.aspectj.lang.JoinPoint;

/**
 * @author: zph
 * @date: 2024/02/22
 * @description: 锁超时处理策略
 */
public interface LockTimeoutHandler {

    void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint);
}
