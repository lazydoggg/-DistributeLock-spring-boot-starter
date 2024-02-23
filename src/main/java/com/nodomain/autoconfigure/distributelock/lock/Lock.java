package com.nodomain.autoconfigure.distributelock.lock;

/**
 * @author: zph
 * @date: 2024/02/22
 * @description: 锁顶级接口
 */
public interface Lock {

    boolean acquire();

    boolean release();

}
