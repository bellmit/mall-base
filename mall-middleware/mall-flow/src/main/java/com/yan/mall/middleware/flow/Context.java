package com.yan.mall.middleware.flow;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huyan on 2022/1/4.
 * TIME: 14:51
 * DESC:
 */
public class Context {

    private Map<String, Object> resultMap = new HashMap<>();

    public Map<String, Object> getAdaptorMap() {
        return resultMap;
    }

    public void setAdaptorMap(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
    }
}
