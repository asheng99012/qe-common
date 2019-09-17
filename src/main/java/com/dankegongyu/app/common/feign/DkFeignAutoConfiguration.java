package com.dankegongyu.app.common.feign;

import com.dankegongyu.app.common.*;
import com.dankegongyu.app.common.log.LogFilter;
import com.dankegongyu.app.common.log.RecordRpcLog;
import com.google.common.base.Splitter;
import feign.Client;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.cloud.sleuth.instrument.web.client.feign.TraceFeignClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
@AutoConfigureAfter({TraceFeignClientAutoConfiguration.class})
public class DkFeignAutoConfiguration {
    @Bean
    public DkFeignContext.DKFeignContextBeanPostProcessor dkFeignContextBeanPostProcessor(BeanFactory beanFactory) {
        return new DkFeignContext.DKFeignContextBeanPostProcessor(beanFactory);
    }


    @Bean
    FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }

    @Bean
    @ConditionalOnBean(CachingSpringLoadBalancerFactory.class)
    public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,
                              SpringClientFactory clientFactory) {
        return new LoadBalancerFeignClient(new DKLoadBalancerFeignClient.DefaultClient(null, null), cachingFactory, clientFactory);
    }

}
