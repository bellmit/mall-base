package com.yan.mall.middleware.redis.annotation;

import com.yan.mall.middleware.redis.config.RedissonConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by huyan on 2021/11/24.
 * TIME: 9:11
 * DESC:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import({RedissonConfig.class})
public @interface EnableMallRedisson {


}
