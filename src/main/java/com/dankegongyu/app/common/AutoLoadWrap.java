package com.dankegongyu.app.common;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoLoadWrap<T> extends LazyLoadBase {
    Class klass;
    private static ThreadLocal<Map<Class, List<String>>> map = new ThreadLocal();

    public static <T> T warp(Class klass) {
        AutoLoadWrap wrap = new AutoLoadWrap();
        wrap.klass = klass;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(klass);
        enhancer.setCallback(wrap);
        enhancer.setClassLoader(klass.getClassLoader());
        return (T) enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        addMethod(method.getName());
        return null;
    }

    private static Map<Class, List<String>> getMap() {
        Map<Class, List<String>> config = map.get();
        if (config == null) {
            config = new HashMap<>();
            map.set(config);
        }
        return map.get();
    }

    public static List<String> getAutoLoadMethod(Class klass) {
        List<String> list = getMap().get(klass);
        map.remove();
        return list;
    }

    private void addMethod(String methodName) {
        List<String> list = getMap().get(klass);
        if (list == null) {
            list = new ArrayList<>();
            getMap().put(klass, list);
        }
        list.add(methodName);
    }
}
