package com.dankegongyu.app.common.httpRpc;

import com.dankegongyu.app.common.JsonUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PostForm<T, E> extends Base<T, E> {
    private static Logger logger = LoggerFactory.getLogger(PostForm.class);

    public HttpEntity getFormEntity()throws IOException{
        AbstractHttpEntity formEntity = null;
        formEntity = new UrlEncodedFormEntity(getData(), "utf-8");
        formEntity.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
        return formEntity;
    }

    public T exec() {

        try {

            HttpPost request = new HttpPost(getUrl());
            request.setEntity(this.getFormEntity());
            Map<String, String> headers = getHeaders();
            if (null != headers) {
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    request.addHeader(e.getKey(), e.getValue());
                }
            }
            String ret = getEntity(request);
            return (T) dealResult(ret);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        } catch (ClientProtocolException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            this.remove();
        }
        return null;
    }

    public List<NameValuePair> getData() {
        Object data = prepareData();
        setRequestBody(data);
        Map querys = new HashMap();
        if (data != null)
            querys = JsonUtils.convert(data, Map.class);
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        if (null != querys) {
            for (Object key : querys.keySet()) {
                nameValuePairList.add(new BasicNameValuePair(key.toString(), querys.get(key).toString()));
            }
        }
        return nameValuePairList;
    }


}
