package com.dankegongyu.app.common.mq;

import com.dankegongyu.app.common.*;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.util.Date;

public abstract class BaseListener implements ChannelAwareMessageListener {
    Logger logger = LoggerFactory.getLogger(BaseListener.class);
    @Autowired
    RpcService rpcService;


    public String getBody(Message message) {
        try {
            return new String(message.getBody(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        Long start = new Date().getTime();
        try {

            if (message.getMessageProperties() != null && message.getMessageProperties().getHeaders() != null && message.getMessageProperties().getHeaders().get(CurrentContext.class.getName()) != null) {
                CurrentContext.resetFromJson(message.getMessageProperties().getHeaders().get(CurrentContext.class.getName()).toString());
            }
            TraceIdUtils.setTraceId();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
            logger.info("from mq receive channel:{}", JsonUtils.toJson(channel));
            logger.info("from mq receive params:{}", JsonUtils.toJson(message.getMessageProperties()));
            logger.info("from mq receive data:{}", getBody(message));
            exec(message, channel);
        } catch (Exception ex) {
            logger.error("from mq handler error:{}", ex.getMessage(), ex);
            AppUtils.getBean(DeadLetterListener.class).toDeadQueue(message, channel, ex);
        } finally {
            CurrentContext.clear();
            Long costTime = new Date().getTime() - start;
            logger.info("cost time:{}", costTime);
        }

    }

    public abstract void exec(Message message, Channel channel);
}
