package com.dankegongyu.app.common.filterChain;

public interface Filter<T, R> {
    R doFilter(T request, FilterChain<T, R> chain);
}
