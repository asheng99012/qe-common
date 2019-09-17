package com.dankegongyu.app.common.dkmq;

import com.danke.infra.mq.common.Msg;
import com.danke.infra.mq.common.producer.MessageTemplate;
import com.dankegongyu.app.common.Current;
import com.dankegongyu.app.common.CurrentContext;
import com.dankegongyu.app.common.JsonUtils;
import com.dankegongyu.app.common.TraceIdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

public class Sender {
    @Autowired
    private MessageTemplate messageTemplate;

    @Value("${message.config.defaultExchange}")
    public String defaultExchange;
    @Value("${message.config.proxyRoutingKey}")
    public String proxyRoutingKey;

    public void send(String routingKey, Object msg) {
        send(defaultExchange, routingKey, msg, getDefaultHeaders());
    }

    public void send(String exchange, String routingKey, Object msg) {
        send(exchange, routingKey, msg, getDefaultHeaders());
    }

    public void send(String exchange, String routingKey, Object msg, Map<String, Object> headers) {
        //todo msg需要添加自定义header
        messageTemplate.sendMessage(
                Msg.builder().topic(exchange).tag(routingKey).payload(msg).build()
        );
    }

    public static Map<String, Object> getDefaultHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("traceId", TraceIdUtils.getTraceId());
        headers.put(CurrentContext.class.getName(), CurrentContext.toJson());
        headers.put("execCount", 1);
        return headers;
    }
}
