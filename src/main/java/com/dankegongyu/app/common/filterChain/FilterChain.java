package com.dankegongyu.app.common.filterChain;

import com.dankegongyu.app.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FilterChain<T, R> {
    Logger logger = LoggerFactory.getLogger(FilterChain.class);
    protected T request;
    protected List<Filter<T, R>> additionalFilters;
    protected int size = 0;
    protected int currentPosition = 0;

    public FilterChain() {
    }

    public FilterChain(T request, List<Filter<T, R>> additionalFilters) {
        this.request = request;
        this.additionalFilters = additionalFilters;
        this.size = additionalFilters.size();
        logger.info("生成责任链,链条个数为{}，参数为：{}", size, JsonUtils.toJson(request));

    }

    public boolean hasNext() {
        return size > currentPosition;
    }

    public R doFilter() {
        if (currentPosition == size) {
            logger.info("责任链执行完毕");
            return null;
        } else {
            this.currentPosition++;
            Filter nextFilter = additionalFilters.get(currentPosition - 1);
            logger.info("{}开始执行", nextFilter.getClass());
            Object ret = nextFilter.doFilter(request, this);
            logger.info("{}结果为：{}", nextFilter.getClass(), JsonUtils.toJson(ret));
            return (R) ret;
        }
    }

}
