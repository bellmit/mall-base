package com.yan.mall.middleware.flow;

/**
 * Created by huyan on 2022/1/4.
 * TIME: 15:22
 * DESC:
 */
public interface FlowNodeInterface<T> {
    /**
     * 执行node方法
     */
    T invokeNode(RunData data, Context context);

    /**
     * node执行完之后调用的方法
     */
    void afterInvoke(RunData data, Context context);

    /**
     * 从context中获取此node结果的key
     */
    String resultKey();
}
