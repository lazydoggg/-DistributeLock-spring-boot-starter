package com.nodomain.autoconfigure.distributelock.handler.release;

import com.nodomain.autoconfigure.distributelock.model.LockInfo;

/**
 * @author: zph
 * @date: 2024/02/22
 * @description:
 */
public interface ReleaseTimeoutHandler {

    void handle(LockInfo lockInfo);
}
