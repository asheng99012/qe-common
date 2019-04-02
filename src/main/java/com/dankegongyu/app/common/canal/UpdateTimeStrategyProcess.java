package com.dankegongyu.app.common.canal;

import com.dankegongyu.app.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface UpdateTimeStrategyProcess extends Process {
    static Map<Class, UpdateTimeStrategy> strategyMap = new ConcurrentHashMap<>();
    Logger logger = LoggerFactory.getLogger(UpdateTimeStrategyProcess.class);

    default boolean canDoProcess(Message message) {
        if (!strategyMap.containsKey(this.getClass())) {
            UpdateTimeStrategy strategy = AnnotationUtils.getAnnotation(getClass(), UpdateTimeStrategy.class);
            if (strategy == null) {
                throw new RuntimeException("请添加 UpdateTimeStrategy 注解");
            }
            strategyMap.put(getClass(), strategy);
        }
        UpdateTimeStrategy strategy = strategyMap.get(getClass());
        if (!message.getData().containsKey(strategy.key())) {
            throw new RuntimeException("字段【" + strategy.key() + "】不存在");
        }
        if (!(message.getData().get(strategy.key()) instanceof Date))
            throw new RuntimeException("字段【" + strategy.key() + "】不是时间类型");
        long tt = (new Date().getTime() - ((Date) message.getData().get(strategy.key())).getTime()) / 1000;
        if (tt > strategy.interval()) {
            logger.error("数据【" + JsonUtils.toJson(message) + "】已过期【" + tt + "】秒");
            return false;
        }
        return true;
    }
}
