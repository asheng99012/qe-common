package com.dankegongyu.app.common;

import org.slf4j.MDC;

public class TraceIdUtils {
    static String TRACEID = "traceId";
    static String SERVERIP = "serverIp";

    public static void setTraceId(String traceId) {
        CurrentContext.set(TRACEID, traceId);
        MDC.put(TRACEID, traceId);
        MDC.put(SERVERIP, Current.getLocalIP());
    }

    public static void setTraceId() {
        if (CurrentContext.get(TRACEID) == null)
            setTraceId(UUID19.randomUUID());
        else
            setTraceId(CurrentContext.get(TRACEID));
    }

    public static String getTraceId() {
        if (CurrentContext.get(TRACEID) != null)
            return CurrentContext.get(TRACEID);
        return MDC.get(TRACEID);

    }

    public static String getTraceIdAndInit() {
        if (MDC.get(TRACEID) == null) {   //获得并初始化
            setTraceId(UUID19.randomUUID());
        }
        return MDC.get(TRACEID);
    }

    public static void clear() {
        CurrentContext.set(TRACEID, null);
    }
}
