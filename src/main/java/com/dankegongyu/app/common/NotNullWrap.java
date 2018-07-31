package com.dankegongyu.app.common;

import com.dankegongyu.app.common.exception.NeedEmailException;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class NotNullWrap<T> implements MethodInterceptor {
    private static Map<String, Field> fieldMap = new HashMap<>();
    private static Map<String, Method> mhMap = new HashMap<>();

    protected T target;
    public T proxy;

    public T getTarget() {
        return target;
    }

    public T createProxy() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(this);
        enhancer.setClassLoader(target.getClass().getClassLoader());
        proxy = (T) enhancer.create();
        return proxy;
    }

    public void load(String... fileds) {
        String filed;
        try {
            for (int i = 0; i < fileds.length; i++) {
                filed = fileds[i];
                setAndGetValue("get" + filed.substring(0, 1).toUpperCase() + filed.substring(1));
            }
        } catch (Throwable e) {
            throw new NeedEmailException(e.getMessage(), e.getCause());
        }

    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object result = methodProxy.invoke(this.target, objects);
        if (result == null && method.getName().startsWith("get")) {
            result = setAndGetValue(method.getName());
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

    public Method getMH(String methodName) throws Throwable {
        String key = target.getClass() + "." + methodName;
        if (!mhMap.containsKey(key)) {
            Method mymethod = this.getClass().getMethod(methodName);
            mhMap.put(key, mymethod);
        }
        return mhMap.get(key);
    }

    public Object setAndGetValue(String methodName) throws Throwable {
        String filedName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        Object result = getMH(methodName).invoke(this);
        getTargetField(filedName).set(target, result);
        return result;
    }
}
