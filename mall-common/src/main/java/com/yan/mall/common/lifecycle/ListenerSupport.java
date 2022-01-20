package com.yan.mall.common.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ListenerSupport implements BeanFactoryAware, BeanFactoryPostProcessor {
    private ConfigurableListableBeanFactory beanFactory;
    private ConcurrentHashMap<String, List<Object>> cached = new ConcurrentHashMap<>();

    public <T> List<T> getAll(Class<T> clazz) {
        String key = clazz.getName();
        return (List<T>) cached.computeIfAbsent(key, k -> {
            String[] names = beanFactory.getBeanNamesForType(clazz);
            List<T> objs = new ArrayList<>(names.length);
            for (String name : names) {
                objs.add(beanFactory.getBean(name, clazz));
            }
            AnnotationAwareOrderComparator.sort(objs);
            return Collections.unmodifiableList(objs);
        });
    }

    public <T> List<T> get(Class<T> clazz, boolean priorityOrdered, boolean ordered) {
        String key = clazz.getName() + "_" + priorityOrdered + "_" + ordered;
        return (List<T>) cached.computeIfAbsent(key, k -> {
            String[] names = beanFactory.getBeanNamesForType(clazz);
            List<T> objs = new ArrayList<>(names.length / 3);
            for (String name : names) {
                boolean matchPriority = beanFactory.isTypeMatch(name, PriorityOrdered.class);
                boolean mathOrdered = beanFactory.isTypeMatch(name, Ordered.class);

                if ((priorityOrdered && matchPriority)
                        || (!matchPriority && ordered && mathOrdered)
                        || (!priorityOrdered && !ordered && !matchPriority && !mathOrdered)) {
                    objs.add(beanFactory.getBean(name, clazz));
                }
            }
            OrderComparator.sort(objs);
            return Collections.unmodifiableList(objs);
        });
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (null == this.beanFactory && beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}