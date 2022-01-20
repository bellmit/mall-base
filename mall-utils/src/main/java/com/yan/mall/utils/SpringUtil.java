package com.yan.mall.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by huyan on 2021/12/8.
 * TIME: 8:21
 * DESC:
 */
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtil.context == null) {
            SpringUtil.context = applicationContext;
        }
    }

    //通过name获取 Bean.
    public static Object getBean(String name) {
        return context.getBean(name);
    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }

    // 获取当前环境
    public static String getActiveProfile() {
        return context.getEnvironment().getActiveProfiles()[0];
    }
}
