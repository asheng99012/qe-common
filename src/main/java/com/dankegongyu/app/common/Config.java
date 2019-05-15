package com.dankegongyu.app.common;

import com.dankegongyu.app.common.feign.*;
import com.dankegongyu.app.common.log.LogFilter;
import com.dankegongyu.app.common.log.RecordRpcLog;
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
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Import({FeignFilter.class})
public class Config {
    @Autowired
    private Environment environment;

    @Bean
    public FilterRegistrationBean current() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new Current());
        registration.addUrlPatterns("/*");
        if (environment != null && environment.getProperty("filter.current.exclude") != null)
            registration.addInitParameter("exclude", environment.getProperty("filter.current.exclude"));
        registration.setName("current");
        return registration;
    }

    @Bean
    public FilterRegistrationBean logFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new LogFilter());
        registration.addUrlPatterns("/*");
        if (environment != null && environment.getProperty("filter.logFilter.exclude") != null)
            registration.addInitParameter("exclude", environment.getProperty("filter.logFilter.exclude"));
        if (environment != null && environment.getProperty("filter.logFilter.recordResult") != null)
            registration.addInitParameter("recordResult", environment.getProperty("filter.logFilter.recordResult"));
        registration.setName("logFilter");
        return registration;
    }


    @Bean
    AppUtils appUtils() {
        return new AppUtils();
    }

    @Bean
    FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }

    @Bean
//    @ConditionalOnBean(Mock.class)
    public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,
                              SpringClientFactory clientFactory) {
        return new LoadBalancerFeignClient(new DKLoadBalancerFeignClient.DefaultClient(null, null), cachingFactory, clientFactory);
    }

    @Bean(name = "springSessionDefaultRedisSerializer")
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }


    @Bean(name = "localLog")
    @ConditionalOnMissingBean(name = "localLog")
    public RecordRpcLog localLog() {
        return new RecordRpcLog.Default();
    }

    @Bean(name = "rpcLog")
    @ConditionalOnMissingBean(name = "rpcLog")
    public RecordRpcLog rpcLog() {
        return new RecordRpcLog.Default();
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

    @Bean
    @ConditionalOnProperty(prefix = "commRpc", name = "locations")
    public CommRpcDefinitionRegistryPostProcessor qeormBeanDefinitionRegistryPostProcessor(Environment env) {
        return new CommRpcDefinitionRegistryPostProcessor(env);
    }

    @Bean
    @ConditionalOnProperty(prefix = "commRpc", name = "enable")
    public CommRpc commRpc() {
        return new CommRpc();
    }


}
