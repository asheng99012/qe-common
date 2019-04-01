package com.dankegongyu.app.common.canal;

import com.dankegongyu.app.common.AppUtils;
import com.dankegongyu.app.common.mq.BaseListener;
import com.dankegongyu.app.common.mq.Proxy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CanalListener extends BaseListener implements ApplicationListener<ContextRefreshedEvent> {
    Logger logger = LoggerFactory.getLogger(CanalListener.class);
    static Map<Filter, List<Process>> processMap;
    static Map<String, List<Process>> tableMap = Maps.newConcurrentMap();

    private String errorRoutingKey;

    public void setErrorRoutingKey(String errorRoutingKey) {
        this.errorRoutingKey = errorRoutingKey;
    }

    @Override
    public void exec(Message message, Channel channel) {
        String body = getBody(message);
        List<com.dankegongyu.app.common.canal.Message> listMsg = com.dankegongyu.app.common.canal.Message.fromJson(body);
        String filter = listMsg.get(0).getDatabase() + "." + listMsg.get(0).getTable();
        List<Process> list = getProcess(filter);
        for (Process process : list) {
            doProcess(process, listMsg);
        }
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        processMap = Maps.newConcurrentMap();
        Map<String, Object> beansWithAnnotationMap = AppUtils.getApplicationContext().getBeansWithAnnotation(Filter.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotationMap.entrySet()) {
            Object bean = entry.getValue();
            Filter value = AnnotationUtils.getAnnotation(bean.getClass(), Filter.class);
            if (value != null) {
                List<Process> proList;
                if (processMap.containsKey(value)) {
                    proList = processMap.get(value);
                } else {
                    proList = Lists.newArrayList();
                    processMap.put(value, proList);
                }
                proList.add((Process) bean);
            }
        }
    }

    public List<Process> getProcess(String tableName) {
        if (!tableMap.containsKey(tableName)) {
            List<Process> list = new ArrayList<>();
            for (Map.Entry<Filter, List<Process>> entry : processMap.entrySet()) {
                Filter filter = entry.getKey();
                if (filter.table().equals(tableName) || tableName.matches(filter.filter())) {
                    list.addAll(entry.getValue());
                }
            }
            tableMap.put(tableName, list);
        }
        return tableMap.get(tableName);
    }

    void doProcess(Process process, List<com.dankegongyu.app.common.canal.Message> msgList) {
        for (com.dankegongyu.app.common.canal.Message message : msgList) {
            try {
                process.doProcess(message);
            } catch (Exception e) {
                logger.error(e.getMessage(), e.getCause());
                Proxy.proxy(process.getClass(), errorRoutingKey).doProcess(message);
            }
        }
    }
}
