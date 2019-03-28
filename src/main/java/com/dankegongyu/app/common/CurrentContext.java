package com.dankegongyu.app.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.core.NamedThreadLocal;

import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;

public class CurrentContext {
    private static ThreadLocal<Map<String, Object>> context = new NamedThreadLocal<>(CurrentContext.class.getName());

    public static Map<String, Object> getContext() {
        Map<String, Object> map = context.get();
        if (map == null) {
            map = new HashMap<String, Object>();
            context.set(map);
        }
        return map;
    }

    public static void setContext(Map<String, Object> map) {
        context.set(map);
    }

    public static void set(String key, Object val) {
        getContext().put(key, val);
    }

    public static <T> T get(String key, Object defaultVal) {
        Map<String, Object> map = getContext();
        if (!map.containsKey(key)) {
            map.put(key, defaultVal);
        }
        return (T) map.get(key);
    }

    public static <T> T get(String key) {
        return get(key, null);
    }

    public static String toJson() {
        return JSON.toJSONString(getContext(), SerializerFeature.WriteClassName);
    }

    public static void resetFromJson(String json) {
        setContext(JSON.parseObject(json, Map.class));
    }

}
