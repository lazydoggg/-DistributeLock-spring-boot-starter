package com.nodomain.autoconfigure.distributelock.handler.release;

import com.nodomain.autoconfigure.distributelock.handler.DistributeLockTimeoutException;
import com.nodomain.autoconfigure.distributelock.model.LockInfo;

/**
 * @author: zph
 * @date: 2024/02/22
 * @description: 释放超时策略
 */
public enum ReleaseTimeoutStrategy implements ReleaseTimeoutHandler{


    /**
     * 继续执行业务逻辑，不做任何处理
     */
    NO_OPERATION() {
        @Override
        public void handle(LockInfo lockInfo) {
            // do nothing
        }
    },
    /**
     * 快速失败
     */
    FAIL_FAST() {
        @Override
        public void handle(LockInfo lockInfo) {

            String errorMsg = String.format("Found Lock(%s) already been released while lock lease time is %d s", lockInfo.getName(), lockInfo.getLeaseTime());
            throw new DistributeLockTimeoutException(errorMsg);
        }
    }

}
