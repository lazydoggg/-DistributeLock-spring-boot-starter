package com.nodomain.autoconfigure.distributelock.lock;

import com.nodomain.autoconfigure.distributelock.model.LockInfo;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;



/**
 * @author: zph
 * @date: 2024/02/22
 * @description: 锁工厂
 */
public class LockFactory {

    Logger logger= LoggerFactory.getLogger(getClass());

    @Autowired
    private RedissonClient redissonClient;

    public Lock getLock(LockInfo lockInfo){//获取锁对象，其实就是redisson的客户端对象RedissonClient
        switch (lockInfo.getType()) {
            case Reentrant:
                return new ReentrantLock(redissonClient, lockInfo);
            case Fair:
                return new FairLock(redissonClient, lockInfo);
            case Read:
                return new ReadLock(redissonClient, lockInfo);
            case Write:
                return new WriteLock(redissonClient, lockInfo);
            default:
                return new ReentrantLock(redissonClient, lockInfo);
        }
    }

}
