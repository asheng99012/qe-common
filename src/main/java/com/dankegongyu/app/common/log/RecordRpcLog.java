package com.dankegongyu.app.common.log;

import com.dankegongyu.app.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public interface RecordRpcLog {
    /**
     * @param traceId
     * @param childTraceId
     * @param start
     * @param end
     * @param type
     * @param dataId
     * @param fromIp
     * @param targetUrl
     * @param httpMethod
     * @param header
     * @param params
     * @param toIp
     * @param isError
     * @param headerStatus
     * @param result
     */
    void record(String traceId, String childTraceId, Date start, Date end, String type, String dataId, String fromIp, String targetUrl, String httpMethod, Map header, Map params, String toIp, boolean isError, int headerStatus, Object result);

    public static class Default implements RecordRpcLog {
        Logger logger = LoggerFactory.getLogger(Default.class);

        @Override
        public void record(String traceId, String childTraceId, Date start, Date end, String type, String dataId, String fromIp, String targetUrl, String httpMethod, Map header, Map params, String toIp, boolean isError, int headerStatus, Object result) {
            //fromIp
            logger.info("从[{}]发起到[{}]的请求[{}],参数为：[{}]，返回值是[{}]，耗时[{}] "
                    , fromIp, toIp, targetUrl, JsonUtils.toJson(params), JsonUtils.toJson(result), end.getTime() - start.getTime());
        }
    }
}
