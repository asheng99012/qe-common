package com.dankegongyu.app.common.log;

import lombok.Builder;
import lombok.Data;
import qeorm.MongodbModelBase;

import java.util.Date;


@Data
@Builder
public class RequestLog extends MongodbModelBase {
    public String traceId;
    public String childTraceId;
    public Date start;
    public Date end;
    public String type;
    public String dataId;
    public String fromIp;
    public String targetUrl;
    public String httpMethod;
    public Object header;
    public Object params;
    public String toIp;
    public Boolean isError;
    public Integer headerStatus;
    public Object result;
    public Integer cost;

    public static RequestLog build(String traceId, String childTraceId, Date start, Date end, String type, String dataId, String fromIp, String targetUrl, String httpMethod, Object header, Object params, String toIp, Boolean isError, Integer headerStatus, Object result, Integer cost) {
      return   (RequestLog.builder().traceId(traceId).childTraceId(childTraceId).start(start).end(end).type(type).dataId(dataId).fromIp(fromIp).targetUrl(targetUrl).httpMethod(httpMethod).header(header)).params(params).toIp(toIp).isError(isError).headerStatus(headerStatus).result(result).cost(cost).build();
    }

}
