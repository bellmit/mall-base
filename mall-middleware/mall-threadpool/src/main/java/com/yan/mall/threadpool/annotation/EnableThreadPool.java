package com.yan.mall.threadpool.annotation;

import com.yan.mall.threadpool.config.ThreadPoolConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by huyan on 2021/12/1.
 * TIME: 21:43
 * DESC:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import({ThreadPoolConfig.class})
public @interface EnableThreadPool {
}
