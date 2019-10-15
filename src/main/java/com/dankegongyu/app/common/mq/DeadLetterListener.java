package com.dankegongyu.app.common.mq;

import com.alibaba.fastjson.JSON;
import com.dankegongyu.app.common.AppUtils;
import com.google.common.base.Strings;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class DeadLetterListener extends BaseListener {

    @Autowired
    Sender sender;
    private String deadRoutingKey;
    private String terminatedRoutingKey;
    private int maxCount = 10;
    private Map<String, Integer> routingKeyMaxCount = new HashMap<>();

    public void setTerminatedRoutingKey(String terminatedRoutingKey) {
        this.terminatedRoutingKey = terminatedRoutingKey;
    }

    public void setDeadRoutingKey(String deadRoutingKey) {
        this.deadRoutingKey = deadRoutingKey;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public void setRoutingKeyMaxCount(Map<String, Integer> routingKeyMaxCount) {
        this.routingKeyMaxCount = routingKeyMaxCount;
    }

    @Override
    public void exec(Message message, Channel channel) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        Object msg = getBody(message);
        try {
            msg = JSON.parseObject(msg.toString());
        } catch (Exception e) {
        }
        sender.send(headers.get("originRoutingKey").toString(), msg, headers);
    }

    public void toDeadQueue(Message message, Channel channel, Exception e) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        if (!headers.containsKey("originRoutingKey")) {
            headers.put("originRoutingKey", message.getMessageProperties().getReceivedRoutingKey());
        }
        headers.put("errorMsg", e.getMessage());
        int execCount = 2;
        if (headers.containsKey("execCount")) {
            execCount = (Integer) headers.get("execCount");
            execCount++;
        }
        headers.put("execCount", execCount);
        Object msg = getBody(message);
        try {
            msg = JSON.parseObject(msg.toString());
        } catch (Exception ex) {
        }
        if (execCount > getMaxCount(headers.get("originRoutingKey").toString())) {
            if (!Strings.isNullOrEmpty(terminatedRoutingKey)) {
                sender.send(terminatedRoutingKey, msg, headers);
            }
            if (AppUtils.getBean(Mqlog.class) != null) {
                AppUtils.getBean(Mqlog.class).log(message, "2", false, e.getMessage() + ":" + ExceptionUtils.getStackTrace(e));
            }
        } else {
            sender.send(deadRoutingKey, msg, headers);
        }
    }

    public int getMaxCount(String routingKey) {
        if (routingKeyMaxCount.containsKey(routingKey))
            return routingKeyMaxCount.get(routingKey);
        return maxCount;
    }
}
