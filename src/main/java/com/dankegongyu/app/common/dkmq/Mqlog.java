package com.dankegongyu.app.common.dkmq;

import com.dankegongyu.app.common.JsonUtils;
import com.dankegongyu.app.common.TraceIdUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import qeorm.SqlExecutor;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class Mqlog {
    @Value("${mqlog.tableName}")
    private String tableName;
    @Value("${mqlog.dbName}")
    private String dbName;
    @Value("${mqlog.exclude}")
    private String exclude;
    @Value("${mqlog.appNamme}")
    private String appNamme;

    public void log(String key, String exchange, String routingkey, String ip, Object message, Object ext, int exeCount, String type, String status, String result, Date StartDeliverTime) {
        String sql = "insert into " + tableName + " (`traceid`,`key`,`exchange`,`routingkey`,`ip`,`message`,`exeCount`,`create_at`,`type`,`status`,`result`,`StartDeliverTime`,`appNamme`) values({traceid},{key},{exchange},{routingkey},{ip},{message},{exeCount},{create_at},{type},{status},{result},{StartDeliverTime},{appNamme})";
        Map param = new HashMap() {{
            put("traceid", TraceIdUtils.getTraceId());
            put("key", key);
            put("exchange", exchange);
            put("routingkey", routingkey);
            put("ip", ip);
            put("message", message);
            put("ext", ext);
            put("exeCount", exeCount);
            put("create_at", new Date());
            put("type", type);
            put("status", status);
            put("result", result);
            put("StartDeliverTime", StartDeliverTime);
            put("appNamme", appNamme);
        }};
        if (isExclude(param.get("routingkey").toString())) return;
        try {
            SqlExecutor.execSql(sql, param, Integer.class, dbName);
        } catch (Exception e) {
            log.error(sql + ":" + JsonUtils.toJson(param) + ":" + e.getMessage(), e);
        }
    }


    boolean isExclude(String queue) {
        return (";" + exclude + ";").contains(";" + queue + ";");
    }


    public List<Map> getList(Map param) {
        String sql = "select * from " + tableName + " where traceid={traceid} and key={key} and exchange={exchange} and routingkey={routingkey} and type={type} and status={status} and create_at>={create_at_b} and create_at<={create_at_e} and StartDeliverTime>={StartDeliverTime_b} and StartDeliverTime<={StartDeliverTime_e} and appNamme={appNamme} order by create_at desc";
        return SqlExecutor.execSql(sql, param, Map.class, dbName);
    }
}
