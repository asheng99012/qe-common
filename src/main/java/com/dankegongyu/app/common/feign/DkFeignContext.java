package com.dankegongyu.app.common.feign;

import feign.Client;
import feign.Request;
import feign.Response;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class DkFeignContext extends FeignContext {
    private final FeignContext delegate;
    private BeanFactory beanFactory;
    private CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory;

    private Object springClientFactory;

    DkFeignContext(FeignContext delegate) {
        this.delegate = delegate;
    }

    DkFeignContext(FeignContext delegate, BeanFactory beanFactory) {
        this.delegate = delegate;
        this.beanFactory = beanFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(String name, Class<T> type) {
        T object = this.delegate.getInstance(name, type);
        return (T) dkwrap(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getInstances(String name, Class<T> type) {
        Map<String, T> instances = this.delegate.getInstances(name, type);
        if (instances == null) {
            return null;
        }
        Map<String, T> convertedInstances = new HashMap<>();
        for (Map.Entry<String, T> entry : instances.entrySet()) {
            convertedInstances.put(entry.getKey(), (T) dkwrap(entry.getValue()));
        }
        return convertedInstances;
    }

    public Object dkwrap(Object bean) {
        if (bean instanceof LoadBalancerFeignClient && !(bean instanceof DKLoadBalancerFeignClient)) {
            Client client = new DKLoadBalancerFeignClient((LoadBalancerFeignClient) bean, factory(), (SpringClientFactory) clientFactory());
            return new DKLoadBalancerFeignClient(client, factory(), (SpringClientFactory) clientFactory());
        }
        return bean;
    }

    private CachingSpringLoadBalancerFactory factory() {
        if (this.cachingSpringLoadBalancerFactory == null) {
            this.cachingSpringLoadBalancerFactory = this.beanFactory
                    .getBean(CachingSpringLoadBalancerFactory.class);
        }
        return this.cachingSpringLoadBalancerFactory;
    }

    private Object clientFactory() {
        if (this.springClientFactory == null) {
            this.springClientFactory = this.beanFactory
                    .getBean(SpringClientFactory.class);
        }
        return this.springClientFactory;
    }


    public static class DKFeignContextBeanPostProcessor implements BeanPostProcessor {

        private final BeanFactory beanFactory;

        public DKFeignContextBeanPostProcessor(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName)
                throws BeansException {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName)
                throws BeansException {
            if (bean instanceof Proxy) {
                String name = Proxy.getInvocationHandler(bean).getClass().getTypeName();
                if (name.equals("feign.ReflectiveFeign$FeignInvocationHandler"))
                    return DKLoadBalancerFeignClient.FeiginProxy.proxy((Proxy) bean);
                if (name.equals("com.danke.arch.commons.metrics.feign.MetricInvocationHandler"))
                    return DKLoadBalancerFeignClient.FeiginProxy.proxy((Proxy) bean);
            }
            if (bean instanceof FeignContext && !(bean instanceof DkFeignContext)) {
                return new DkFeignContext((FeignContext) bean, beanFactory);
            }
            return bean;
        }
    }
}
