package com.dankegongyu.app.common.dkmq;

import com.alibaba.fastjson.JSON;
import com.dankegongyu.app.common.AppUtils;
import com.dankegongyu.app.common.CurrentContext;
import com.dankegongyu.app.common.JsonUtils;
import com.dankegongyu.app.common.RpcService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Proxy implements MethodInterceptor {
    static Boolean _isAsync;
    private Class targetClass;
    private String routingkey;

    public static <T> T proxy(T obj) {
        return proxy((Class<T>) obj.getClass());
    }

    public static <T> T proxy(Class<T> klass) {
        return proxy(klass, AppUtils.getBean(Sender.class).proxyRoutingKey);
    }

    public static <T> T proxy(T obj, String key) {
        return proxy((Class<T>) obj.getClass(), key);
    }

    public static <T> T proxy(Class<T> klass, String key) {
        klass = (Class<T>) ClassUtils.getUserClass(klass);
        Proxy proxy = new Proxy();
        proxy.targetClass = klass;
        proxy.routingkey = key;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(klass);
        enhancer.setCallback(proxy);
        enhancer.setClassLoader(klass.getClassLoader());
        return (T) enhancer.create();
    }

    private static boolean isAsync() {
        if (_isAsync == null) {
            Boolean ret = AppUtils.getBean(Environment.class).getProperty("proxy.async", Boolean.class);
            if (ret != null)
                _isAsync = ret;
            else _isAsync = true;
        }
        return _isAsync;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (isAsync()) {
            AppUtils.getBean(Sender.class).send(this.routingkey, ProxyStruct.builder().klass(targetClass.getName()).method(method.getName()).data(objects).build());
            return null;
        } else {
            return AppUtils.getBean(RpcService.class).run(new Object[]{
                            targetClass.getName(), method.getName()
                    },
                    JSON.parseArray(JsonUtils.toJson(objects)).toArray());
        }

    }

    public static Object exec(ProxyStruct struct) {
        try {
            return AppUtils.getBean(RpcService.class).run(new Object[]{
                            struct.klass, struct.method
                    },
                    JSON.parseArray(JsonUtils.toJson(struct.data)).toArray());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProxyStruct {
        private String klass;
        private String method;
        private Object data;
    }
}
