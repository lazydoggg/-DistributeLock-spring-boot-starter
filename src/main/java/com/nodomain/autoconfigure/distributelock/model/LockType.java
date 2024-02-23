package com.nodomain.autoconfigure.distributelock.model;

/**
 * @author: zph
 * @date: 2024/02/22
 * @description: 锁类型
 */
public enum LockType {

    /**
     * 可重入锁
     */
    Reentrant,
    /**
     * 公平锁
     */
    Fair,
    /**
     * 读锁
     */
    Read,
    /**
     * 写锁
     */
    Write;

    LockType() {
    }

}
