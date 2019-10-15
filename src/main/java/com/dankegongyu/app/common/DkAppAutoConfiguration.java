package com.dankegongyu.app.common;

import com.dankegongyu.app.common.canal.DkCanalListener;
import com.dankegongyu.app.common.dkmq.MQAspect;
import com.dankegongyu.app.common.dkmq.MqConfiguration;
import com.dankegongyu.app.common.dkmq.Mqlog;
import com.dankegongyu.app.common.dkmq.Sender;
import com.dankegongyu.app.common.feign.*;
import com.dankegongyu.app.common.log.LogFilter;
import com.dankegongyu.app.common.log.RecordRpcLog;
import com.dankegongyu.app.common.xxl.XxlAspect;
import com.google.common.base.Splitter;
import com.xxl.job.core.handler.IJobHandler;
import feign.Client;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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

import java.util.List;

@Configuration
public class DkAppAutoConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    @Order(-1)
    @ConditionalOnProperty(name = "cosAllow")
    public FilterRegistrationBean cosFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new CosFilter());
        registration.addUrlPatterns("/*");
        if (environment != null && environment.getProperty("filter.cosFilter.allow.header") != null)
            registration.addInitParameter("allowHeader", environment.getProperty("filter.cosFilter.allow.header"));
        registration.setName("cosFilter");
        return registration;
    }

    @Bean
    public FilterRegistrationBean current() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new Current());
        registration.addUrlPatterns("/*");
        if (environment != null && environment.getProperty("filter.current.exclude") != null)
            registration.addInitParameter("exclude", environment.getProperty("filter.current.exclude"));
        if (environment != null && environment.getProperty("filter.current.isApi") != null)
            registration.addInitParameter("isApi", environment.getProperty("filter.current.isApi"));
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
    @ConditionalOnProperty(prefix = "filter.internal", name = "notAllowDomain")
    public FilterRegistrationBean internalFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new InternalFilter());
        if (environment != null && environment.getProperty("filter.internal.notAllowDomain") != null)
            registration.addInitParameter("notAllowDomain", environment.getProperty("filter.internal.notAllowDomain"));
        if (environment != null && environment.getProperty("filter.internal.urlPatterns") != null) {
            List<String> allowUrls = Splitter.on(",").splitToList(environment.getProperty("filter.internal.urlPatterns"));
            for (String url : allowUrls) {
                registration.addUrlPatterns(url);
            }
        }
        registration.setName("internalFilter");
        return registration;
    }

    @Bean
    public FilterRegistrationBean feignFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new FeignFilter());
        registration.addUrlPatterns("/*");
        registration.setName("feignFilter");
        return registration;
    }

    @Bean
    AppUtils appUtils() {
        return new AppUtils();
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

    @Bean
    public DkCanalListener dkCanalListener() {
        return new DkCanalListener();
    }

    @Bean
    @ConditionalOnClass(IJobHandler.class)
    public XxlAspect xxlAspect() {
        return new XxlAspect();
    }
}
