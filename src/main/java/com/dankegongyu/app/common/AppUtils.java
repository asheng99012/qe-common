package com.dankegongyu.app.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AppUtils {
    private static ApplicationContext appContext = null;

    @Autowired
    public void setApplicationContext(ApplicationContext paramApplicationContext) {
        AppUtils.appContext = paramApplicationContext;
    }

    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
        if (AppUtils.appContext == null)
            AppUtils.appContext = Current.getApplicationContext();
        return AppUtils.appContext;
    }

    //通过name获取 Bean.
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }


    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}
