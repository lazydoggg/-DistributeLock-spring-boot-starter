package com.nodomain.autoconfigure.distributelock.lock;

import com.nodomain.autoconfigure.distributelock.model.LockInfo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author: zph
 * @date: 2024/02/22
 * @description: 可重入锁
 */
public class ReentrantLock implements Lock{

    private RLock rLock;

    private final LockInfo lockInfo;

    private RedissonClient redissonClient;

    public ReentrantLock(RedissonClient redissonClient,LockInfo lockInfo) {
        this.redissonClient = redissonClient;
        this.lockInfo = lockInfo;
    }
    @Override
    public boolean acquire() {
        try {
            rLock = redissonClient.getLock(lockInfo.getName());
            return rLock.tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean release() {
        //判断持有锁的线程是否是当前线程
        if(rLock.isHeldByCurrentThread()){
            try {
                return rLock.forceUnlockAsync().get();
            } catch (InterruptedException e) {
                return false;
            } catch (ExecutionException e) {
                return false;
            }
        }
        return false;
    }
    public String getKey(){
        return this.lockInfo.getName();
    }
}
