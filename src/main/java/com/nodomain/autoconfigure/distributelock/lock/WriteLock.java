package com.nodomain.autoconfigure.distributelock.lock;

import com.nodomain.autoconfigure.distributelock.model.LockInfo;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author: zph
 * @date: 2024/02/22
 * @description: 互斥锁
 */
public class WriteLock implements Lock{

    private RReadWriteLock rLock;

    private final LockInfo lockInfo;

    private RedissonClient redissonClient;

    public WriteLock(RedissonClient redissonClient,LockInfo info) {
        this.redissonClient = redissonClient;
        this.lockInfo = info;
    }

    @Override
    public boolean acquire() {
        try {
            rLock=redissonClient.getReadWriteLock(lockInfo.getName());
            return rLock.writeLock().tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean release() {
        if(rLock.writeLock().isHeldByCurrentThread()){
            try {
                return rLock.writeLock().forceUnlockAsync().get();
            } catch (InterruptedException e) {
                return false;
            } catch (ExecutionException e) {
                return false;
            }
        }

        return false;
    }

}
