package com.yan.mall.middleware.flow;

import lombok.AllArgsConstructor;

import java.util.concurrent.Callable;

/**
 * Created by huyan on 2022/1/4.
 * TIME: 15:25
 * DESC:
 */
@AllArgsConstructor
public class NodeExecuteTask<T> implements Callable<T> {

    private FlowNodeInterface<T> flowNodeInterface;
    private RunData data;
    private Context context;

    public T execute() {
        try {
            T result = flowNodeInterface.invokeNode(data, context);
            flowNodeInterface.afterInvoke(data, context);
            return result;
        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    @Override
    public T call() throws Exception {
        return execute();
    }
}
