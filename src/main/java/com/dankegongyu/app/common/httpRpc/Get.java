package com.dankegongyu.app.common.httpRpc;

import com.dankegongyu.app.common.JsonUtils;
import com.google.common.base.Charsets;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Get<T, E> extends Base<T, E> {
    private static Logger logger = LoggerFactory.getLogger(Get.class);

    public T exec() {
        String ret = getEntity(creatRequest());
        return (T) dealResult(ret);
    }


    public HttpGet creatRequest() {
        HttpGet request = new HttpGet(buildUrl());
        Map<String, String> headers = getHeaders();
        if (null != headers) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        return request;
    }

    public String buildUrl() {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(getUrl());
        Object data = prepareData();
        Map querys = new HashMap();
        if (data != null)
            querys = JsonUtils.convert(data, Map.class);
        if (null != querys && querys.keySet().size() > 0) {

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (Object key : querys.keySet()) {
                nvps.add(new BasicNameValuePair(key.toString(), querys.get(key).toString()));
            }
            String parametersFormat = URLEncodedUtils.format(nvps, Charsets.UTF_8);
            sbUrl.append("?").append(parametersFormat);
        }
        return sbUrl.toString();
    }
}
