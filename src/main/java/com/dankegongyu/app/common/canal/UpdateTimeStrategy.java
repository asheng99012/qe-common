package com.dankegongyu.app.common.canal;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdateTimeStrategy {
    //字段名
    String key();

    //区间长度，单位 秒
    long interval();
}
