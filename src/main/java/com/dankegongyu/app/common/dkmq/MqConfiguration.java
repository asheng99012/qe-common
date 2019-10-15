package com.dankegongyu.app.common.dkmq;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "mqlog", name = "enabled")
    public Mqlog mqlog() {
        return new Mqlog();
    }

    @Bean
    @ConditionalOnProperty(prefix = "message.config", name = "defaultExchange")
    public Sender dkmqSender() {
        return new Sender();
    }

    @Bean
    @ConditionalOnClass(com.danke.infra.mq.common.producer.MessageTemplate.class)
    public MQAspect mqAspect() {
        return new MQAspect();
    }


}
