package com.dankegongyu.app.common.log;

import com.dankegongyu.app.common.JsonUtils;
import com.dankegongyu.app.common.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import qeorm.SqlExecutor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RequestLog implements RecordRpcLog {
    @Value("${rpclog.tableName}")
    private String tableName;
    @Value("${rpclog.dbName}")
    private String dbName;
    @Value("${rpclog.appNamme}")
    private String appNamme;

    @Override
    public void record(String traceId, String childTraceId, Date start, Date end, String type, String dataId, String fromIp, String targetUrl, String httpMethod, Map header, Map params, String toIp, boolean isError, int headerStatus, Object result) {
        log.info("[{}]从[{}]发起到[{}]的请求[{}],参数为：[{}]，返回值是[{}]，耗时[{}] "
                , type, fromIp, toIp, targetUrl, JsonUtils.toJson(params), JsonUtils.toJson(result), end.getTime() - start.getTime());
        if (type.startsWith("com.danke.bill.common.rpc")) return;
        String sql = "insert into " + tableName +
                " (`traceid`,`childTraceId`,`start_at`,`end_at`,`type`,`dataId`,`fromIp`,`targetUrl`,`httpMethod`,`header`,`paramter`,`toIp`,`isError`,`headerStatus`,`result`,`cost`,`appNamme`) " +
                "values({traceid},{childTraceId},{start_at},{end_at},{type},{dataId},{fromIp},{targetUrl},{httpMethod},{header},{paramter},{toIp},{isError},{headerStatus},{result},{cost},{appNamme})";
        Map param = new HashMap() {{
            put("traceid", TraceIdUtils.getTraceId());
            put("childTraceId", childTraceId);
            put("start_at", start);
            put("end_at", end);
            put("type", type);
            put("dataId", dataId);
            put("fromIp", fromIp);
            put("targetUrl", targetUrl);
            put("httpMethod", httpMethod);
            put("header", header);
            put("paramter", params);
            put("toIp", toIp);
            put("isError", isError ? 1 : 2);
            put("headerStatus", headerStatus);
            put("result", result);
            put("cost", end.getTime() - start.getTime());
            put("appNamme", appNamme);
        }};
        try {
            SqlExecutor.execSql(sql, param, Integer.class, dbName);
        } catch (Exception e) {
            log.error(sql + ":" + JsonUtils.toJson(param) + ":" + e.getMessage(), e);
        }
    }

    public List<Map> getList(Map param) {
        String sql = "select * from " + tableName + " where appNamme={appNamme} and traceid={traceid} and start_at>={start_at} and end_at <={end_at} and type={type}  and isError={isError} order by start_at desc";
        List<Map> ret = SqlExecutor.execSql(sql, param, Map.class, dbName);
        if (ret != null && ret.size() > 0) {
            ret.forEach(map -> {
                map.put("paramter", JsonUtils.toJson(map.get("paramter")));
                map.put("result", JsonUtils.toJson(map.get("result")));
            });
        }
        return ret;
    }
}
