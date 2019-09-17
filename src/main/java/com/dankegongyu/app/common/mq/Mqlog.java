package com.dankegongyu.app.common.mq;

import com.dankegongyu.app.common.TraceIdUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import qeorm.SqlExecutor;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
public class Mqlog {
    private String tableName;
    private String dbName;
    private String excludeQueue;

    public void log(Message message, String type, boolean status, String result) {
        String sql = "insert into " + tableName + " (`traceid`,`key`,`exchange`,`routingkey`,`queue`,`message`,`exeCount`,`create_at`,`type`,`status`,`result`) values({traceid},{key},{exchange},{routingkey},{queue},{message},{exeCount},{create_at},{type},{status},{result})";
        String msg = getBody(message);
        MessageProperties properties = message.getMessageProperties();
        int execCount = 1;
        if (properties.getHeaders().containsKey("execCount")) {
            execCount = (Integer) properties.getHeaders().get("execCount");
        }
        int finalExecCount = execCount;
        Map param = new HashMap() {{
            put("traceid", TraceIdUtils.getTraceId());
            put("key", Md5Crypt.apr1Crypt(msg, ""));
            put("exchange", properties.getReceivedExchange());
            put("routingkey", properties.getReceivedRoutingKey());
            put("queue", properties.getConsumerQueue());
            put("message", msg);
            put("exeCount", finalExecCount);
            put("create_at", new Date());
            put("type", type);
            put("status", status);
            put("result", result);
        }};
        if (isExclude(param.get("queue").toString())) return;
        try {
            SqlExecutor.execSql(sql, param, Integer.class, dbName);
        } catch (Exception e) {
            log.error(sql + ":" + e.getMessage(), e);
        }
    }

    String getBody(Message message) {
        try {
            return new String(message.getBody(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    boolean isExclude(String queue) {
        return (";" + excludeQueue + ";").contains(";" + queue + ";");
    }
}
