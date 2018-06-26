package com.dankegongyu.app.common;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.springframework.cglib.beans.BeanMap;

import java.util.Iterator;

public class Wrap {
    private Object target;

    public static Wrap getWrap(Object obj) {
        Wrap wrap = new Wrap();
        wrap.target = obj;
        return wrap;
    }

    public <T> T getValue(String path) {
        Iterator<String> iterator = Splitter.on(".").split(path).iterator();
        String filed;
        Object ret = target;
        while (ret != null && iterator.hasNext()) {
            filed = iterator.next();
            ret = getValue(ret, filed);
        }
        return (T) ret;
    }

    private Object getValue(Object obj, String filed) {
        BeanMap map = BeanMap.create(obj);
        Object ret = null;
        if (map.containsKey(filed))
            ret = map.get(filed);
        return ret;
    }
}
