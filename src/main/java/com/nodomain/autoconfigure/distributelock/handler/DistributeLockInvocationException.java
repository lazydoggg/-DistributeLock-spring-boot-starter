package com.nodomain.autoconfigure.distributelock.handler;

/**
 * @author: zph
 * @date: 2024/02/22
 * @description: 调用方法异常
 */
public class DistributeLockInvocationException extends RuntimeException{

    public DistributeLockInvocationException() {
    }

    public DistributeLockInvocationException(String message) {
        super(message);
    }

    public DistributeLockInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

}
