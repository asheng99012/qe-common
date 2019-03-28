package com.dankegongyu.app.common.filterChain;

public interface FilterEnhancer<T, R> extends Filter<T, R> {
    @Override
    default R doFilter(T request, FilterChain<T, R> chain) {
        R ret = process(request);
        return doNextOrReturnDefaultValue(ret, chain);
    }

    R process(T request);

    default R doNextOrReturnDefaultValue(R defaultValue, FilterChain<T, R> chain) {
        if (isNeedDoNext(defaultValue) && chain != null && chain.hasNext()) {
            return chain.doFilter();
        } else {
            return defaultValue;
        }
    }

    boolean isNeedDoNext(R val);
}
