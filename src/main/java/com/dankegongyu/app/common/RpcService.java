package com.dankegongyu.app.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Service
public class RpcService {
    private static Map<String, KV> pairMap = new HashMap<>();

    Logger logger = LoggerFactory.getLogger(RpcService.class);

    public Object run(Object[] fun, Object[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        KV kv = getObjectInfo(fun[0].toString(), fun[1].toString(), args.length);
        Object instanse = kv.getKey();
        Method method = kv.getVal();
        Type[] paramsTypes = method.getGenericParameterTypes();
        Object[] params = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            params[i] = args[i] == null ? null : JsonUtils.convert(args[i], paramsTypes[i]);
        }
        logger.info(instanse.getClass().getName() + "@" + method.getName());
        logger.info(JsonUtils.toJson(params));
        Object ret = method.invoke(instanse, params);
        return ret;
    }

    private Object getInstance(Class<?> klass) throws IllegalAccessException, InstantiationException {
        Object instance;
        try {
            instance = AppUtils.getBean(klass);
        } catch (NoSuchBeanDefinitionException e) {
            instance = klass.newInstance();
        }
        return instance;
    }

    private KV getObjectInfo(String className, String methodName, int argc) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        String action = className + "." + methodName;
        if (!pairMap.containsKey(action)) {
            Object instanse = getInstance(Class.forName(className));
            Method[] methods = instanse.getClass().getMethods();
            Method method = null;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName) && methods[i].getParameterTypes().length == argc) {
                    method = methods[i];
                    break;
                }
            }
            pairMap.put(action, new KV(instanse, method));
        }
        return pairMap.get(action);
    }

    private static class KV {
        private Object key;
        private Method val;

        public KV(Object key, Method val) {
            this.key = key;
            this.val = val;
        }

        public Object getKey() {
            return key;
        }

        public Method getVal() {
            return val;
        }

    }
}
