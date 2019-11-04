package com.dankegongyu.app.common.dkmq;

import com.danke.infra.mq.common.Msg;
import com.danke.infra.mq.common.producer.SendMessageResult;
import com.dankegongyu.app.common.*;
import com.dankegongyu.app.common.exception.BusinessException;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Aspect
@Slf4j
public class MQAspect {

    @Autowired(required = false)
    private Mqlog mqlog;

    @Around("execution (* com.danke.infra.mq.common.producer.MessageTemplate.sendMessage(..))")
    public Object sendMsg(ProceedingJoinPoint point) throws Throwable {
        //todo msg需要添加自定义header
        Msg msg = null;
        if (point instanceof MethodInvocationProceedingJoinPoint) {
            MethodInvocationProceedingJoinPoint _p = (MethodInvocationProceedingJoinPoint) point;
            if (!_p.getTarget().getClass().getName().equals("com.danke.infra.mq.common.producer.MessageTemplate"))
                return point.proceed();
            msg = (Msg) _p.getArgs()[0];
        } else {
            msg = (Msg) point.getArgs()[0];
        }
        msg.putUserProperties(CurrentContext.class.getName(), CurrentContext.toJson());
        String errmsg = "";
        String key = "";
        try {
            SendMessageResult ret = (SendMessageResult) point.proceed();
            key = ret.getMessageId();
            return ret;
        } catch (Throwable t) {
            errmsg = t.getMessage() + ":" + ExceptionUtils.getStackTrace(t);
            log.error(errmsg,t);
            throw new BusinessException(JsonUtils.toJson(msg) + ":" + t.getMessage(), t.getCause());
        } finally {
            log.info("发送数据：" + JsonUtils.toJson(msg));
            TraceIdUtils.clear();
            if (mqlog != null) {
                mqlog.log(key, msg.getTopic(), msg.getTag(), msg.getBornHost(),
                        msg.getPayload(), msg, 0, "com.danke.infra.mq.common.producer.MessageTemplate",
                        Strings.isNullOrEmpty(errmsg) ? "suuc" : "error", errmsg, new Date(msg.getStartDeliverTime()));
            }
        }
    }


    @Around("@annotation(com.danke.infra.mq.common.annotation.Message)")
    public Object consumer(ProceedingJoinPoint point) {
        //todo 解析 自定义header
        Msg msg = null;
        String type = "";
        if (point instanceof MethodInvocationProceedingJoinPoint) {
            MethodInvocationProceedingJoinPoint _p = (MethodInvocationProceedingJoinPoint) point;
            msg = (Msg) _p.getArgs()[0];
            type = _p.getTarget().getClass().getName() + "." + _p.getSignature().getName();
        } else {
            msg = (Msg) point.getArgs()[0];
        }

        if (msg.getUserProperties(CurrentContext.class.getName()) != null) {
            CurrentContext.resetFromJson(msg.getUserProperties(CurrentContext.class.getName()));
        }
        TraceIdUtils.setTraceId();

        String errmsg = "";
        if (msg.getPayload() instanceof Proxy.ProxyStruct) {
            Proxy.ProxyStruct struct = (Proxy.ProxyStruct) msg.getPayload();
            type = struct.getKlass() + "." + struct.getMethod();
        }
        try {
            Object ret = point.proceed();
            return ret;
        } catch (Throwable t) {
            errmsg = t.getMessage() + ":" + ExceptionUtils.getStackTrace(t);
            throw new BusinessException(JsonUtils.toJson(msg) + ":" + t.getMessage(), t.getCause());
        } finally {
            log.info("消费数据：" + JsonUtils.toJson(msg) + ",结果：" + errmsg);
            TraceIdUtils.clear();
            if (mqlog != null) {

                mqlog.log(msg.getMsgId(), msg.getTopic(), msg.getTag(),
                        Current.getNewLocalIP(), msg.getPayload(), msg, msg.getReconsumeTimes(), type,
                        Strings.isNullOrEmpty(errmsg) ? "suuc" : "error", errmsg, new Date(msg.getStartDeliverTime()));
            }
        }
    }
}
