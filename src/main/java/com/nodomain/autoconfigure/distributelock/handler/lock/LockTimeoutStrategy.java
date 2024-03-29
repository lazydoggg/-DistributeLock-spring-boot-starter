package com.nodomain.autoconfigure.distributelock.handler.lock;

import com.nodomain.autoconfigure.distributelock.handler.DistributeLockTimeoutException;
import com.nodomain.autoconfigure.distributelock.lock.Lock;
import com.nodomain.autoconfigure.distributelock.model.LockInfo;
import org.aspectj.lang.JoinPoint;

import java.util.concurrent.TimeUnit;

/**
 * @author: zph
 * @date: 2024/02/22
 * @description: 锁超时枚举
 */
public enum LockTimeoutStrategy implements LockTimeoutHandler{


    /**
     * 继续执行业务逻辑，不做任何处理
     */
    NO_OPERATION() {
        @Override
        public void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint) {
            // do nothing
        }
    },

    /**
     * 快速失败
     */
    FAIL_FAST() {
        @Override
        public void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint) {

            String errorMsg = String.format("Failed to acquire Lock(%s) with timeout(%ds)", lockInfo.getName(), lockInfo.getWaitTime());
            throw new DistributeLockTimeoutException(errorMsg);
        }
    },

    /**
     * 一直阻塞，直到获得锁，在太多的尝试后，仍会报错
     */
    KEEP_ACQUIRE() {

        private static final long DEFAULT_INTERVAL = 100L;

        private static final long DEFAULT_MAX_INTERVAL = 3 * 60 * 1000L;

        @Override
        public void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint) {

            long interval = DEFAULT_INTERVAL;

            while(!lock.acquire()) {

                if(interval > DEFAULT_MAX_INTERVAL) {
                    String errorMsg = String.format("Failed to acquire Lock(%s) after too many times, this may because dead lock occurs.",
                            lockInfo.getName());
                    throw new DistributeLockTimeoutException(errorMsg);
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(interval);
                    interval <<= 1;
                } catch (InterruptedException e) {
                    throw new DistributeLockTimeoutException("Failed to acquire Lock", e);
                }
            }
        }
    }

}
