package com.dankegongyu.app.common.concurrency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 并发上下文
 */
public class ConcurrencyContext<K, T> {
    private Map<K, K> register;
    private Map<K, T> result;
    long timeout;

    public ConcurrencyContext(long timeout) {
        this.timeout = timeout;
        register = new ConcurrentHashMap();
        result = new ConcurrentHashMap();
    }


    public boolean exist(K k) {
        if (register.containsKey(k))
            return true;
        register.put(k, k);
        return false;
    }

    public Map<K, T> getResult() {
        return result;
    }

    public long getTimeout() {
        return timeout;
    }
}
