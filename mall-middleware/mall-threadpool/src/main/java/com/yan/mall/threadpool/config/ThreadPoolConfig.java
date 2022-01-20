package com.yan.mall.threadpool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by huyan on 2021/5/31.
 * TIME: 15:46
 * DESC:
 */
@Configuration
public class ThreadPoolConfig {

    //线程计数器
    private static AtomicInteger threadIdx = new AtomicInteger(0);

    @Bean
    public ThreadPoolExecutor createExecutor() {
        /**
         * 核心线程个数:
         * Runtime.getRuntime().availableProcessors() : 获取cpu核心线程数等同于计算资源
         * CPU密集型服务推荐设置该值为N，也就是核心线程数=cpu的线程数，这样可以尽量避免线程的上下文切换
         * IO密集型服务推荐设置该值为2N（2N是根据服务压测算出来，如果不涉及业务就设置2N即可）
         */
        Integer corePoolSize = Runtime.getRuntime().availableProcessors();

        /**
         * 最大线程个数
         * CPU密集型服务该值可以设置小一些
         * IO密集型服务可以将该值设置大一些
         */
        Integer maximumPoolSize = Runtime.getRuntime().availableProcessors() * 2;

        Integer keepAliveTime = 60;
        TimeUnit unit = TimeUnit.SECONDS;

        /**
         * 阻塞队列，设置有界阻塞队列
         */
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(512);

        /**
         * 拒绝策略
         */
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                queue,
                threadFactoryBean(),
                handler);

        return executor;
    }

    /**
     * 自定义线程工厂类，可以设置一些属性方便debug
     * @return
     */
    private ThreadFactory threadFactoryBean() {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setName("MALL-THREAD-POOL-" + threadIdx.getAndIncrement());
                return thread;
            }
        };
    }
}
