package com.dankegongyu.app.common;

import com.dankegongyu.app.common.exception.NeedEmailException;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class NotNullWrap<T> extends LazyLoadBase {

    public static <T> T warp(T obj) {
        return (T) (new NotNullWrap().createProxy(obj));
    }

    public T createProxy(Class klass) {
        return createProxy(null, klass);
    }

    public T createProxy(T obj) {
        return createProxy(obj, obj.getClass());
    }

    public T createProxy(T obj, Class klass) {
        this.target = obj;
        Enhancer enhancer = new Enhancer();
        klass = getRealClass(klass);
        enhancer.setSuperclass(klass);
        enhancer.setCallback(this);
        enhancer.setClassLoader(klass.getClassLoader());
        return (T) enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (method.getName().startsWith("get")) {
            Object ret = null;
            if (this.target != null) {
                ret = methodProxy.invoke(this.target, objects);
            }
            if (ret == null) {
                Class klass = RealClass.getRealClass(method.getReturnType().getName());
                if (klass.getName().startsWith("java."))
                    ret = null;
                else
                    ret = createProxy(klass);
            }
            return ret;
        } else {
            return methodProxy.invoke(this.target, objects);
        }
    }
}
