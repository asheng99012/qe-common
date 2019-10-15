package com.dankegongyu.app.common.mq;

import com.dankegongyu.app.common.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class Proxy implements MethodInterceptor {
    Logger logger = LoggerFactory.getLogger(Proxy.class);
    private Class targetClass;
    private String routingkey;

    public static <T> T proxy(T obj) {
        return proxy((Class<T>) obj.getClass());
    }

    public static <T> T proxy(Class<T> klass) {
        return proxy(klass, AppUtils.getBean(Sender.class).defaultRoutingKey);
    }

    public static <T> T proxy(T obj, String key) {
        return proxy((Class<T>) obj.getClass(), key);
    }

    public static <T> T proxy(Class<T> klass, String key) {
        Proxy proxy = new Proxy();
        proxy.targetClass = klass;
        proxy.routingkey = key;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(klass);
        enhancer.setCallback(proxy);
        enhancer.setClassLoader(klass.getClassLoader());
        return (T) enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Map data = new HashMap();
        data.put("class", targetClass.getName());
        data.put("method", method.getName());
        data.put("data", objects);
        AppUtils.getBean(Sender.class).send(this.routingkey, data);
        return null;
    }
}
