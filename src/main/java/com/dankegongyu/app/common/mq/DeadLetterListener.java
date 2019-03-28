package com.dankegongyu.app.common.mq;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class DeadLetterListener extends BaseListener {

    @Autowired
    Sender sender;
    private String deadRoutingKey;
    private int maxCount;
    private Map<String, Integer> routingKeyMaxCount;

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

    public void toDeadQueue(Message message, Channel channel) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        if (!headers.containsKey("originRoutingKey")) {
            headers.put("originRoutingKey", message.getMessageProperties().getReceivedRoutingKey());
        }
        int execCount = 2;
        if (headers.containsKey("execCount")) {
            execCount = (Integer) headers.get("execCount");
            execCount++;
        }
        if (execCount > getMaxCount(headers.get("originRoutingKey").toString())) {
            //todo
        }
        headers.put("execCount", execCount);

        Object msg = getBody(message);
        try {
            msg = JSON.parseObject(msg.toString());
        } catch (Exception e) {
        }
        sender.send(deadRoutingKey, msg, headers);
    }

    public int getMaxCount(String routingKey) {
        if (routingKeyMaxCount.containsKey(routingKey))
            return routingKeyMaxCount.get(routingKey);
        return maxCount;
    }
}
