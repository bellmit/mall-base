package com.yan.mall.middleware.redis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by huyan on 2021/12/8.
 * TIME: 8:37
 * DESC:
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedRLock {

    String lockName() default "";

    String lockKey() default "";

    long timeout() default 60;

    String errMsg() default "处理中，请勿重复操作";
}
