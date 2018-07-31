package com.dankegongyu.app.common;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LazyLoadBase<T> implements MethodInterceptor {
    private static Map<String, Field> fieldMap = new HashMap<>();
    private static Map<String, Method> mhMap = new HashMap<>();

    protected T target;

    public T getTarget() {
        return target;
    }

    public T createProxy() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(this);
        enhancer.setClassLoader(target.getClass().getClassLoader());
        return (T) enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object result = methodProxy.invoke(this.target, objects);
        if (result == null && method.getName().startsWith("get")) {
            result = setAndGetValue(method);
        }
        return result;
    }

    public Field getTargetField(String filedName) throws NoSuchFieldException {
        String key = target.getClass() + "." + filedName;
        if (!fieldMap.containsKey(key)) {
            Field field = target.getClass().getDeclaredField(filedName);
            field.setAccessible(true);
            fieldMap.put(key, field);
        }
        return fieldMap.get(key);
    }

    public Method getMH(Method method) throws Throwable {
        String key = target.getClass() + "." + method.getName();
        if (!mhMap.containsKey(key)) {
            Method mymethod = this.getClass().getMethod(method.getName());
            mhMap.put(key, mymethod);
        }
        return mhMap.get(key);
    }

    public Object setAndGetValue(Method method) throws Throwable {
        String filedName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
        Object result = getMH(method).invoke(this);
        getTargetField(filedName).set(target, result);
        return result;
    }
}
