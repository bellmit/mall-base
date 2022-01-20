package com.yan.mall.middleware.flow;

import com.yan.mall.utils.CommonUtil;
import com.yan.mall.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by huyan on 2022/1/4.
 * TIME: 15:03
 * DESC:
 */
@Component
@Slf4j
public class FlowEngine {

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public void execute(FlowNode node, RunData data, Context context) {
        Map<String, List<String>> nodeGroup = groupByGroupName(node);
        Map<String, NodeConf> nodeMap = node.getNodeMap();

        for (String groupName : nodeGroup.keySet()) {
            boolean needThrowExp = false;
            List<String> nodeNameList = nodeGroup.get(groupName);
            if (nodeNameList.size() == 1) {
                //节点只有一个，串行执行
                String nodeName = nodeNameList.get(0);
                FlowNodeInterface flowNodeInterface = (FlowNodeInterface) SpringUtil.getBean(nodeName);
                NodeExecuteTask task = new NodeExecuteTask(flowNodeInterface, data, context);
                try {
                    Object result = task.execute();
                    context.getAdaptorMap().put(flowNodeInterface.resultKey(), result);
                } catch (Exception ex) {
                    needThrowExp = true;
                }
            } else {
                //多个node节点，并行执行
                List<Future> resultList = new ArrayList<>();
                List<String> executeNodeNameList = new ArrayList<>();
                List<NodeExecuteTask> executeNodeList = new ArrayList<>();
                for (String nodeName : nodeNameList) {
                    FlowNodeInterface flowNodeInterface = (FlowNodeInterface) SpringUtil.getBean(nodeName);
                    NodeExecuteTask executeNode = new NodeExecuteTask(flowNodeInterface, data, context);

                    executeNodeList.add(executeNode);
                    executeNodeNameList.add(nodeName);
                    resultList.add(threadPoolExecutor.submit(executeNode));
                }
                log.info("批量提交任务完成...");
                for (int i = 0; i < resultList.size(); i++) {
                    String nodeName = executeNodeNameList.get(i);
                    String nodeKey = groupName + "_" + nodeName;
                    FlowNodeInterface flowNodeInterface = (FlowNodeInterface) SpringUtil.getBean(nodeName);

                    NodeConf nodeConf = nodeMap.get(nodeKey);
                    try {
                        Object result = resultList.get(i).get(nodeConf.getTimeout(), TimeUnit.SECONDS);
                        context.getAdaptorMap().put(flowNodeInterface.resultKey(), result);
                    } catch (ExecutionException ex) {
                        needThrowExp = true;
                    } catch (TimeoutException ex) {
                        needThrowExp = true;
                    } catch (Exception ex) {
                        needThrowExp = true;
                    }
                }
            }
            if (needThrowExp) {
                throw new RuntimeException();
            }
        }
    }

    private Map<String, List<String>> groupByGroupName(FlowNode node) {
        Map<String, List<String>> nodeGroup = new LinkedHashMap<>();

        List<String> nodeNameList;
        for (String nodeKey : node.getNodeList()) {
            String groupName = getGroupName(nodeKey);
            String nodeName = getNodeName(nodeKey);
            if (CommonUtil.isEmpty(groupName)) {
                //单个执行
                nodeNameList = new ArrayList<>();
                nodeNameList.add(nodeName);
                nodeGroup.put(nodeName, nodeNameList);
            } else {
                //批量执行
                nodeNameList = nodeGroup.get(groupName);
                if (nodeNameList == null) {
                    nodeNameList = new ArrayList<>();
                }
                nodeNameList.add(nodeName);
                nodeGroup.put(groupName, nodeNameList);
            }
        }
        return nodeGroup;
    }

    private String getGroupName(String nodeKey) {
        String[] arr = nodeKey.split("_");
        return arr.length == 2 ? arr[0] : null;
    }

    private String getNodeName(String nodeKey) {
        String[] arr = nodeKey.split("_");
        return arr.length == 2 ? arr[1] : arr[0];
    }
}
