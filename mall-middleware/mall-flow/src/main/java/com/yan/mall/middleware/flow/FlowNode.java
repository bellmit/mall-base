package com.yan.mall.middleware.flow;

import com.yan.mall.utils.CommonUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by huyan on 2022/1/4.
 * TIME: 14:53
 * DESC:
 */
public class FlowNode {

    private Map<String, NodeConf> nodeMap = new LinkedHashMap<>();

    public void add(String groupName, Class nodeName, NodeConf conf) {
        String key;
        if (CommonUtil.isNotEmpty(groupName)) {
            key = groupName + "_" + nodeName.getSimpleName();
        } else {
            key = nodeName.getSimpleName();
        }
        if (nodeMap.containsKey(key)) {
            return;
        }
        nodeMap.put(key, conf);
    }

    public void add(Class nodeName, NodeConf conf) {
        add(null, nodeName, conf);
    }

    public void replace(String groupName, Class nodeName, NodeConf conf) {
        String key;
        if (CommonUtil.isNotEmpty(groupName)) {
            key = groupName + "_" + nodeName.getSimpleName();
        } else {
            key = nodeName.getSimpleName();
        }
        nodeMap.put(key, conf);
    }

    public void replace(Class nodeName, NodeConf conf) {
        replace(null, nodeName, conf);
    }

    public void remove(String groupName, Class nodeName) {
        String key;
        if (CommonUtil.isNotEmpty(groupName)) {
            key = groupName + "_" + nodeName.getSimpleName();
        } else {
            key = nodeName.getSimpleName();
        }
        nodeMap.remove(key);
    }

    public void remove(Class nodeName) {
        remove(null, nodeName);
    }

    public Set<String> getNodeList() {
        return nodeMap.keySet();
    }

    public Map<String, NodeConf> getNodeMap() {
        return nodeMap;
    }

    public void setNodeMap(LinkedHashMap<String, NodeConf> nodeMap) {
        this.nodeMap = nodeMap;
    }
}
