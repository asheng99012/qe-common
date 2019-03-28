package com.dankegongyu.app.common.zk;


import com.alibaba.fastjson.JSON;
import com.dankegongyu.app.common.AppUtils;
import com.dankegongyu.app.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Consumer;

public class DkzkConfigInit implements ApplicationListener<ContextRefreshedEvent> {
    Logger logger = LoggerFactory.getLogger(DkzkConfigInit.class);
    @Autowired
    protected DkzkClient zkClient;
    protected String rootKey;

    public String getRootKey() {
        return rootKey;
    }

    public void setRootKey(String rootKey) {
        this.rootKey = rootKey;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            logger.info("开始处理事件:{}", event.getClass().getName());
            process(event);
        }
    }

    public void process(ContextRefreshedEvent event) {
        Map<String, Object> beansWithAnnotationMap = AppUtils.getApplicationContext().getBeansWithAnnotation(DkzkConfig.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotationMap.entrySet()) {
            Object bean = entry.getValue();
            dealKLass(bean);
            dealMethod(bean);
            dealFild(bean);
        }

        zkClient.proxyChangeEvent(rootKey);
    }

    protected void dealKLass(Object bean) {
        DkzkValue value = AnnotationUtils.getAnnotation(bean.getClass(), DkzkValue.class);
        if (value != null) {
            String key = value.value();
            bindEvent(key, (json) -> {
                Map<String, Object> map = JsonUtils.convert(json, Map.class);
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Field field = ReflectionUtils.findField(bean.getClass(), entry.getKey());
                    if (field == null) {
                        continue;
                    }
                    field.setAccessible(true);
                    ReflectionUtils.setField(field, bean, JsonUtils.convert(entry.getValue(), field.getType()));
                }
            });
        }
    }

    protected void dealMethod(Object bean) {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        if (methods != null && methods.length > 0) {
            for (Method method : methods) {
                DkzkValue value = AnnotationUtils.findAnnotation(method, DkzkValue.class);
                if (null != value) {
                    String key = value.value();
                    bindEvent(key, (val) -> {
                        try {
                            method.setAccessible(true);
                            method.invoke(bean, val);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    });
                }
            }
        }
    }

    protected void dealFild(Object bean) {
        Field[] fields = bean.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                DkzkValue value = field.getAnnotation(DkzkValue.class);
                if (value != null) {
                    String key = value.value();
                    bindEvent(key, (val) -> {
                        try {
                            field.setAccessible(true);
                            field.set(bean, JSON.parseObject(val, field.getClass()));
                        } catch (IllegalAccessException e) {
                            logger.error(e.getMessage(), e);
                        }
                    });
                }
            }
        }
    }

    void bindEvent(String key, Consumer<String> function) {
        zkClient.watchChange(rootKey + "/" + key, function);
        String data = zkClient.readNode(rootKey + "/" + key);
        if (data != null) {
            function.accept(data);
        }
    }
}
