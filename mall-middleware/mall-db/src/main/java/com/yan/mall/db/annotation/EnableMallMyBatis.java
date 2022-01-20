package com.yan.mall.db.annotation;

import com.yan.mall.db.config.MyBatisConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by huyan on 2021/12/1.
 * TIME: 21:37
 * DESC:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import({MyBatisConfiguration.class})
public @interface EnableMallMyBatis {

}
