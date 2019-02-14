package com.dankegongyu.app.common.event;

import com.dankegongyu.app.common.AppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public abstract class EventBus<E extends ApplicationEvent> implements ApplicationListener {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean isStart = false;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (AppUtils.getApplicationContext().getParent() == null) {
            if (!isStart) {
                logger.info("开始处理事件:{}", event.getClass().getName());
                isStart = true;
                process((E) event);
            } else {
                logger.info("重复执行事:{}", event.getClass().getName());
            }
        }
    }

    public abstract void process(E event);
}
