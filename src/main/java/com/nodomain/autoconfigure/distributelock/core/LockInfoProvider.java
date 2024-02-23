package com.nodomain.autoconfigure.distributelock.core;

import com.nodomain.autoconfigure.distributelock.annotation.DistributeLock;
import com.nodomain.autoconfigure.distributelock.config.DistributeLockConfig;
import com.nodomain.autoconfigure.distributelock.model.LockType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.nodomain.autoconfigure.distributelock.model.LockInfo;


/**
 * @author: zph
 * @date: 2024/02/22
 * @description: 锁信息处理
 */
public class LockInfoProvider {

    private static final String LOCK_NAME_PREFIX = "lock";
    private static final String LOCK_NAME_SEPARATOR = ".";


    @Autowired
    private DistributeLockConfig distributeLockConfig;

    @Autowired
    private BusinessKeyProvider businessKeyProvider;

    private static final Logger logger = LoggerFactory.getLogger(LockInfoProvider.class);

    LockInfo get(JoinPoint joinPoint, DistributeLock distributeLock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LockType type= distributeLock.lockType();
        String businessKeyName=businessKeyProvider.getKeyName(joinPoint,distributeLock);
        //锁的名字，锁的粒度就是这里控制的
        String lockName = LOCK_NAME_PREFIX + LOCK_NAME_SEPARATOR + getName(distributeLock.name(), signature) + businessKeyName;
        long waitTime = getWaitTime(distributeLock);
        long leaseTime = getLeaseTime(distributeLock);
        //如果占用锁的时间设计不合理，则打印相应的警告提示
        if(leaseTime == -1 && logger.isWarnEnabled()) {
            logger.warn("Trying to acquire Lock({}) with no expiration, " +
                    "DistributeLock will keep prolong the lock expiration while the lock is still holding by current thread. " +
                    "This may cause dead lock in some circumstances.", lockName);
        }
        return new LockInfo(type,lockName,waitTime,leaseTime);
    }

    /**
     * 获取锁的name，如果没有指定，则按全类名拼接方法名处理
     * @param annotationName
     * @param signature
     * @return
     */
    private String getName(String annotationName, MethodSignature signature) {
        if (annotationName.isEmpty()) {
            return String.format("%s.%s", signature.getDeclaringTypeName(), signature.getMethod().getName());
        } else {
            return annotationName;
        }
    }


    private long getWaitTime(DistributeLock lock) {
        return lock.waitTime() == Long.MIN_VALUE ?
                distributeLockConfig.getWaitTime() : lock.waitTime();
    }

    private long getLeaseTime(DistributeLock lock) {
        return lock.leaseTime() == Long.MIN_VALUE ?
                distributeLockConfig.getLeaseTime() : lock.leaseTime();
    }

}
