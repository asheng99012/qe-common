package com.dankegongyu.app.common.httpRpc;

import com.dankegongyu.app.common.JsonUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public abstract class PostJson<T, E> extends Base<T, E> {
    private static Logger logger = LoggerFactory.getLogger(PostJson.class);

    public T exec() {
        try {
            String ret = getEntity(creatRequest());
            return (T) dealResult(ret);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        } catch (ClientProtocolException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public HttpPost creatRequest() {
        StringEntity entity = getData();
        HttpPost request = new HttpPost(getUrl());
        request.setEntity(entity);
        Map<String, String> headers = getHeaders();
        if (null != headers) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        return request;
    }

    public StringEntity getData() {
        Object data = prepareData();
        setRequestBody(data);
        StringEntity entity = new StringEntity(JsonUtils.toJson(data), "utf-8");//解决中文乱码问题
//        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        return entity;
    }

    @Override
    public Map<String, String> getHeaders() {
        return new HashMap<String, String>() {{
            put("Content-Type", "application/json");
        }};
    }
}
