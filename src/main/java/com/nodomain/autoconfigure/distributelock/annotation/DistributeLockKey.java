package com.nodomain.autoconfigure.distributelock.annotation;

/**
 * @author: zph
 * @date: 2024/02/04
 * @description: 方法参数注解
 */
public @interface DistributeLockKey {
    String value() default "";
}
