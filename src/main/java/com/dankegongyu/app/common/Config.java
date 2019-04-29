package com.dankegongyu.app.common;

import com.dankegongyu.app.common.feign.FeignFilter;
import com.dankegongyu.app.common.feign.FeignRequestInterceptor;
import com.dankegongyu.app.common.feign.LoadBalancerFeignClientFilter;
import feign.Client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Import({Current.class, FeignFilter.class})
public class Config {
    @Bean
    AppUtils appUtils() {
        return new AppUtils();
    }

    @Bean
    FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }

    @Bean
    @ConditionalOnBean(Mock.class)
    public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,
                              SpringClientFactory clientFactory) {
        return new LoadBalancerFeignClientFilter(new Client.Default(null, null), cachingFactory, clientFactory);
    }

    @Bean(name = "springSessionDefaultRedisSerializer")
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    //需要跟Current.class配合使用
    @Bean
    public JsonRequestBodyAdviceAdapter jsonRequestBodyAdviceAdapter() {
        return new JsonRequestBodyAdviceAdapter();
    }

    @Bean
    @ConditionalOnProperty(prefix = "mail", name = "host")
    @ConfigurationProperties(prefix = "mail")
    public Mailer mailer() {
        return new Mailer();
    }

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public RedisService redisService() {
        return new RedisService();
    }

    @Bean
    public RpcService rpcService() {
        return new RpcService();
    }
}
