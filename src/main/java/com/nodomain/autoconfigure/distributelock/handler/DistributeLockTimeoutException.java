package com.nodomain.autoconfigure.distributelock.handler;

/**
 * @author: zph
 * @date: 2024/02/22
 * @description: 分布式锁超时异常
 */
public class DistributeLockTimeoutException extends RuntimeException{

    public DistributeLockTimeoutException() {
    }

    public DistributeLockTimeoutException(String message) {
        super(message);
    }

    public DistributeLockTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
