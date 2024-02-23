package com.nodomain.autoconfigure.distributelock.core;

import com.nodomain.autoconfigure.distributelock.annotation.DistributeLock;
import com.nodomain.autoconfigure.distributelock.handler.DistributeLockInvocationException;
import com.nodomain.autoconfigure.distributelock.lock.Lock;
import com.nodomain.autoconfigure.distributelock.lock.LockFactory;
import com.nodomain.autoconfigure.distributelock.model.LockInfo;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: zph
 * @date: 2024/02/04
 * @description: 分布式锁逻辑处理切面
 */
@Aspect
@Component
@Order(0)
public class DistributeAspectHandler {

    private static final Logger logger = LoggerFactory.getLogger(DistributeAspectHandler.class);

    @Autowired
    LockFactory lockFactory;

    @Autowired
    private LockInfoProvider lockInfoProvider;

    private final Map<String,LockRes> currentThreadLock = new ConcurrentHashMap<>();


    @Around(value = "@annotation(distributeLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributeLock distributeLock) throws Throwable {
        //获取锁的信息
        LockInfo lockInfo = lockInfoProvider.get(joinPoint,distributeLock);
        String curentLock = this.getCurrentLockId(joinPoint,distributeLock);
        //存要获取锁的线程的唯一标识
        currentThreadLock.put(curentLock,new LockRes(lockInfo, false));
        //获取redission对象
        Lock lock = lockFactory.getLock(lockInfo);
        //尝试获取锁
        boolean lockRes = lock.acquire();

        //如果获取锁失败了，则进入失败的处理逻辑
        if(!lockRes) {
            if(logger.isWarnEnabled()) {
                logger.warn("Timeout while acquiring Lock({})", lockInfo.getName());
            }
            //如果自定义了获取锁失败的处理策略，则执行自定义的降级处理策略
            if(!StringUtils.isEmpty(distributeLock.customLockTimeoutStrategy())) {

                return handleCustomLockTimeout(distributeLock.customLockTimeoutStrategy(), joinPoint);

            } else {
                //否则执行预定义的执行策略
                //注意：如果没有指定预定义的策略，默认的策略为静默啥不做处理
                distributeLock.lockTimeoutStrategy().handle(lockInfo, lock, joinPoint);
            }
        }

        currentThreadLock.get(curentLock).setLock(lock);
        currentThreadLock.get(curentLock).setRes(true);

        return joinPoint.proceed();
    }

    @AfterReturning(value = "@annotation(distributeLock)")
    public void afterReturning(JoinPoint joinPoint, DistributeLock distributeLock) throws Throwable {
        String curentLock = this.getCurrentLockId(joinPoint,distributeLock);
        releaseLock(distributeLock, joinPoint,curentLock);
        cleanUpThreadLocal(curentLock);
    }

    @AfterThrowing(value = "@annotation(distributeLock)", throwing = "ex")
    public void afterThrowing (JoinPoint joinPoint, DistributeLock distributeLock, Throwable ex) throws Throwable {
        String curentLock = this.getCurrentLockId(joinPoint,distributeLock);
        releaseLock(distributeLock, joinPoint,curentLock);
        cleanUpThreadLocal(curentLock);
        throw ex;
    }

    /**
     * 处理自定义加锁超时
     */
    private Object handleCustomLockTimeout(String lockTimeoutHandler, JoinPoint joinPoint) throws Throwable {

        // prepare invocation context
        Method currentMethod = ((MethodSignature)joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(lockTimeoutHandler, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customLockTimeoutStrategy",e);
        }
        Object[] args = joinPoint.getArgs();

        // invoke
        Object res = null;
        try {
            res = handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new DistributeLockInvocationException("Fail to invoke custom lock timeout handler: " + lockTimeoutHandler ,e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        return res;
    }

    /**
     *  释放锁
     */
    private void releaseLock(DistributeLock distributeLock, JoinPoint joinPoint,String curentLock) throws Throwable {
        LockRes lockRes = currentThreadLock.get(curentLock);
        if(Objects.isNull(lockRes)){
            throw new NullPointerException("Please check whether the input parameter used as the lock key value has been modified in the method, which will cause the acquire and release locks to have different key values and throw null pointers.curentLockKey:" + curentLock);
        }
        if (lockRes.getRes()) {//拿到锁的线程才需要解锁
            boolean releaseRes = currentThreadLock.get(curentLock).getLock().release();//释放锁
            // avoid release lock twice when exception happens below
            lockRes.setRes(false);
            if (!releaseRes) {//如果没有解锁
                handleReleaseTimeout(distributeLock, lockRes.getLockInfo(), joinPoint);
            }
        }
    }

    // avoid memory leak
    private void cleanUpThreadLocal(String curentLock) {//清空key-value
        currentThreadLock.remove(curentLock);
    }

    /**
     * 获取当前锁在map中的key
     * @param joinPoint
     * @param distributeLock
     * @return
     */
    private String getCurrentLockId(JoinPoint joinPoint , DistributeLock distributeLock){
        LockInfo lockInfo = lockInfoProvider.get(joinPoint,distributeLock);
        String curentLock= Thread.currentThread().getId() + lockInfo.getName();
        return curentLock;
    }

    /**
     *  处理释放锁时已超时
     */
    private void handleReleaseTimeout(DistributeLock distributeLock, LockInfo lockInfo, JoinPoint joinPoint) throws Throwable {

        if(logger.isWarnEnabled()) {
            logger.warn("Timeout while release Lock({})", lockInfo.getName());
        }

        if(!StringUtils.isEmpty(distributeLock.customReleaseTimeoutStrategy())) {

            handleCustomReleaseTimeout(distributeLock.customReleaseTimeoutStrategy(), joinPoint);

        } else {
            distributeLock.releaseTimeoutStrategy().handle(lockInfo);
        }

    }

    /**
     * 处理自定义释放锁时已超时
     */
    private void handleCustomReleaseTimeout(String releaseTimeoutHandler, JoinPoint joinPoint) throws Throwable {

        Method currentMethod = ((MethodSignature)joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(releaseTimeoutHandler, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customReleaseTimeoutStrategy",e);
        }
        Object[] args = joinPoint.getArgs();

        try {
            handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new DistributeLockInvocationException("Fail to invoke custom release timeout handler: " + releaseTimeoutHandler, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private class LockRes {

        private LockInfo lockInfo;
        private Lock lock;//redission对象
        private Boolean res;//是否拿到锁

        LockRes(LockInfo lockInfo, Boolean res) {
            this.lockInfo = lockInfo;
            this.res = res;
        }

        LockInfo getLockInfo() {
            return lockInfo;
        }

        public Lock getLock() {
            return lock;
        }

        public void setLock(Lock lock) {
            this.lock = lock;
        }

        Boolean getRes() {
            return res;
        }

        void setRes(Boolean res) {
            this.res = res;
        }

        void setLockInfo(LockInfo lockInfo) {
            this.lockInfo = lockInfo;
        }
    }

}
