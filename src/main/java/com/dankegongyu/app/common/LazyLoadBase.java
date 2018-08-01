package com.dankegongyu.app.common;

import com.dankegongyu.app.common.exception.NeedEmailException;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.util.ClassUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LazyLoadBase<T> implements MethodInterceptor {
    private static Map<String, Field> fieldMap = new HashMap<>();
    private static Map<String, Method> mhMap = new HashMap<>();

    protected T target;
    protected T proxy;

    public T getTarget() {
        return target;
    }

    public T getProxy() {
        return proxy;
    }

    public Class getRealClass(Class klass) {
        if (ClassUtils.isCglibProxyClass(klass)) {
            return ClassUtils.getUserClass(klass);
        }
        return klass;
    }

    public T createProxy() {
        Enhancer enhancer = new Enhancer();
        Class klass = getRealClass(target.getClass());
        enhancer.setSuperclass(klass);
        enhancer.setCallback(this);
        enhancer.setClassLoader(klass.getClassLoader());
        proxy = (T) enhancer.create();
        return proxy;
    }

    public void load() {
        String filed;
        List<String> list = AutoLoadWrap.getAutoLoadMethod(target.getClass());
        try {
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    filed = list.get(i);
                    setAndGetValue(filed);
                }
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

    public Field getTargetField(String methodName) throws NoSuchFieldException {
        String filedName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
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
        getTargetField(methodName).set(target, result);
        return result;
    }

}
