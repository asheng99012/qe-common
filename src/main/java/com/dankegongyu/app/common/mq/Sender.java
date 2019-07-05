package com.dankegongyu.app.common.mq;

import com.dankegongyu.app.common.Current;
import com.dankegongyu.app.common.CurrentContext;
import com.dankegongyu.app.common.JsonUtils;
import com.dankegongyu.app.common.TraceIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class Sender {
    static Logger logger = LoggerFactory.getLogger(Sender.class);
    RabbitTemplate rabbitTemplate;
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(String routingKey, Object msg) {
        send(rabbitTemplate.getExchange(), routingKey, msg);
    }

    public void send(String exchange, String routingKey, Object msg) {
        send(exchange, routingKey, msg, getDefaultHeaders());
    }

    public void send(String routingKey, Object msg, Map<String, Object> headers) {
        send(rabbitTemplate.getExchange(), routingKey, msg, headers);
    }

    public void send(String exchange, String routingKey, Object msg, Map<String, Object> headers) {
        logger.info("mq 发送信息 {}|{}|{}", exchange, routingKey, JsonUtils.toJson(msg));
        rabbitTemplate.convertAndSend(exchange, routingKey, getMessage(msg, headers));
    }

    public Map<String, Object> getDefaultHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("ip", Current.getLocalIP());
        headers.put("traceId", TraceIdUtils.getTraceId());
        headers.put(CurrentContext.class.getName(), CurrentContext.toJson());
        headers.put("execCount", 1);
        return headers;
    }

    public Message getMessage(Object msg, Map<String, Object> headers) {
        MessageProperties properties = new MessageProperties();
        properties.getHeaders().putAll(headers);
        return rabbitTemplate.getMessageConverter().toMessage(msg, properties);
    }

    public static class ConfirmCallback implements RabbitTemplate.ConfirmCallback {

        /**
         * Confirmation callback.
         *
         * @param correlationData 回调的相关数据.
         * @param ack             true for ack, false for nack
         * @param cause           专门给NACK准备的一个可选的原因，其他情况为null。
         */
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            logger.info("mq 发送确认回执 {}|{}|{}", correlationData, ack, cause);
        }
    }

    public static class ReturnCallback implements RabbitTemplate.ReturnCallback {

        /**
         * 实现此方法在basicpublish失败时回调
         * 相当于 ReturnListener的功能。
         * 在发布消息时设置mandatory等于true，
         * 监听消息是否有相匹配的队列，没有时ReturnCallback将执行returnedMessage方法，消息将返给发送者
         */
        @Override
        public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
            logger.error("mq 结果返回 {}|{}|{}|{}|{}", message.toString(), replyCode, replyText, exchange, routingKey);
        }
    }
}
