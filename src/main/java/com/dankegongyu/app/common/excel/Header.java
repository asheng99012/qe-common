package com.dankegongyu.app.common.excel;

@FunctionalInterface
public interface Header {
    public <T> T render(Object obj);
}
