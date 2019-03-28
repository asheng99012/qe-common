package com.dankegongyu.app.common.filterChain;

import com.dankegongyu.app.common.UUID19;
import com.dankegongyu.app.common.concurrency.ConcurrencyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class FilterExecuter<T, R> implements FilterEnhancer<T, R> {
    Logger logger = LoggerFactory.getLogger(FilterExecuter.class);
    public List<Filter<T, R>> list;
    public String name;
    public boolean isSerial;
    public int size;
    public int timeout;

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void init(String name, List<Filter<T, R>> list) {
        this.init(name, list, true);
    }

    public void init(String name, List<Filter<T, R>> list, boolean isSerial) {
        this.name = name;
        this.list = list;
        this.isSerial = isSerial;
        this.size = list.size();
    }

    public R dealResult(Map<String, R> result) {
        return null;
    }

    public Map<String, R> getResult(T request) {
        ConcurrencyManager manager = ConcurrencyManager.build(timeout);
        FilterChain chain = new FilterChain();
        for (int i = 0; i < size; i++) {
            Filter<T, R> filter = list.get(i);
            manager.add(filter.getClass() + "_" + UUID19.randomUUID(), () -> filter.doFilter(request, chain));
        }
        Map<String, R> result = manager.getResult();
        return result;
    }

    @Override
    public R process(T request) {
        if (list == null || list.size() == 0) {
            logger.warn("{}的配置不存在", name);
            return null;
        }
        if (isSerial) {
            FilterChain<T, R> chain = new FilterChain<T, R>(request, list);
            return (R) chain.doFilter();
        } else {
            return dealResult(getResult(request));
        }
    }

    @Override
    public boolean isNeedDoNext(R val) {
        return false;
    }

    public R doFilter(T request) {
        return doFilter(request, new FilterChain<>());
    }
}
