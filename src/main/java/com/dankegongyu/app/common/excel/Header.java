package com.dankegongyu.app.common.excel;

@FunctionalInterface
public interface Header {
    public String render(Object obj);
}
