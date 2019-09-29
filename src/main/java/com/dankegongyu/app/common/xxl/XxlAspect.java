package com.dankegongyu.app.common.xxl;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@Slf4j
public class XxlAspect {
    @Around("execution (* com.xxl.job.core.handler.IJobHandler.execute(..))")
    public Object execute(ProceedingJoinPoint point) throws Throwable {
        XxlLogAppender.startXxlJob();
        try {
            return point.proceed();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            XxlLogAppender.stopXxlJob();
        }
    }
}
