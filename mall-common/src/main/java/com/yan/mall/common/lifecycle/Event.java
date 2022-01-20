package com.yan.mall.common.lifecycle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Event implements Serializable {

    private Map<String, Object> context;

    public Event() {
        context = new HashMap<>();
    }

    public void put(String key, Object value) {
        context.put(key, value);
    }

    public Object get(String key) {
        return context.get(key);
    }
}
