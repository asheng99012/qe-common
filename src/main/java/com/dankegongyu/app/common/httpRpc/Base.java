package com.dankegongyu.app.common.httpRpc;

import com.alibaba.fastjson.JSON;
import com.dankegongyu.app.common.*;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

public abstract class Base<T, E> {
    private Class __klass;
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    // public String  chargingStatus="";  //计费状态
    /**
     * 用于记录加密前的数据
     */
    public ThreadLocal<Map<String, Object>> context = new ThreadLocal<>();

    /**
     * 用户保存当前上下文的数据
     *
     * @return
     */
    public Map<String, Object> getContext() {
        Map<String, Object> map = context.get();
        if (map == null) {
            map = new HashMap<>();
            context.set(map);
        }
        return map;
    }

    /**
     * 返回当前请求的业务标识
     *
     * @return
     */
    public abstract String getDataId();

    /**
     * 设置当前请求参数的明文
     *
     * @param body
     */
    public void setRequestBody(Object body) {
        set("REQUESTBODY", body);
    }

    /**
     * 获取当面请求的明文，用于记录日志
     *
     * @return
     */
    public Object getRequestBody() {
        return get("REQUESTBODY");
    }

    /**
     * 保存当前进程的临时变量
     *
     * @param key
     * @param object
     */
    public void set(String key, Object object) {
        getContext().put(key, object);
    }

    /**
     * 获取当前进程的临时变量
     *
     * @param key
     * @param <M>
     * @return
     */
    public <M> M get(String key) {
        return (M) getContext().get(key);
    }

    /**
     * 清除当前请求的临时变量，防止进程间污染
     */
    public void remove() {
        context.remove();
    }

    /**
     * 构造请求
     * 获取参数
     * 参数加密
     * 设置header
     * 设置ssl证书
     * 发送请求
     * 记录日志
     * 返回结果
     */
    public T send(E initParameter) {
        try {
            setInitParams(initParameter);
            return exec();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            this.remove();
        }
    }

    public abstract T exec();

    /**
     * 设置参数
     */
    public void setInitParams(E params) {
        set("PARAMETER", params);
    }

    public E getInitParams() {
        return (E) get("PARAMETER");
    }

    /**
     * 请求日志是否入库
     *
     * @return
     */
    public boolean logToDb() {
        return true;
    }

    /**
     * 拼接url参数
     *
     * @return
     */
    public abstract String getUrl();

    /**
     * 搜集整理要发送的数据
     */
    public abstract Object prepareData();

    /*
      解析计费状态
     */
    public String chargeStatus(String ret) {
        return "";
    }

    ;

    /**
     * 处理返回的数据
     *
     * @param ret
     * @return
     */
    public T dealResult(String ret) {
        if (ret == null) return null;
        return (T) JSON.parseObject(ret, getGenericClass());
    }

    /**
     * 设置header
     */
    public Map<String, String> getHeaders() {
        return null;
    }

    public CloseableHttpClient createClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        String url = getUrl();
        if (url.startsWith("https://")) {
            SSLContext ctx = null;
            try {
                ctx = SSLContext.getInstance("TLS");
                X509TrustManager tm = new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }

                    public void checkClientTrusted(X509Certificate[] xcs, String str) {
                    }

                    public void checkServerTrusted(X509Certificate[] xcs, String str) {
                    }
                };
                ctx.init(null, new TrustManager[]{tm}, null);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            builder.setSSLContext(ctx);
        }
        CloseableHttpClient client = builder.setMaxConnTotal(getMaxConnTotal()).setDefaultRequestConfig(getRequestConfig()).build();
        return Mock.proxy(client);
    }

    /**
     * 连接池数
     *
     * @return
     */
    public int getMaxConnTotal() {
        return 10;
    }

    /**
     * 配置超时时间
     *
     * @return
     */
    public RequestConfig getRequestConfig() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(1000)   //从连接池中获取连接的超时时间
                //与服务器连接超时时间：httpclient会创建一个异步线程用以创建socket连接，此处设置该socket的连接超时时间
                .setConnectTimeout(60000)
                .setSocketTimeout(120000)               //socket读数据超时时间：从服务器获取响应数据的超时时间
                .build();
        return requestConfig;
    }

    public String decryptResult(String responseText) {
        return responseText;
    }

    public String getEntity(HttpResponse response) {
        try {
            return decryptResult(EntityUtils.toString(response.getEntity(), Charsets.UTF_8));
        } catch (IOException e) {
            logger.error("=================================================================================");
            e.printStackTrace();
        }
        return "";
    }

    public String getEntity(HttpGet request) {
        Date sendRequestAt = new Date();//开始请求时间
        String errorMsg = null;
        String ret = "";
        CloseableHttpClient client = null;
        String url = request.getURI().toString();
        if (url.length() > 200) url = url.substring(0, 200);
        String chargstatus = "";
        try {
            client = createClient();
            ret = getEntity(client.execute(request));
        } catch (IOException e) {
            ret = e.getMessage();
            logger.error(e.getMessage(), e);
            errorMsg = e.getMessage();
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    ret = e.getMessage();
                    logger.error(e.getMessage(), e);
                    errorMsg = e.getMessage();
                }
            }

            writeRpcLog("get", url, ret, errorMsg, sendRequestAt);
        }

        return ret;
    }

    public String getEntity(HttpPost request) throws IOException {
        Date sendRequestAt = new Date();//开始请求时间
        String errorMsg = null;
        String ret = "";
        CloseableHttpClient client = null;
        String url = request.getURI().toString();
        if (url.length() > 200) url = url.substring(0, 200);
        String chargstatus = "";
        try {
            client = createClient();
            ret = getEntity(client.execute(request));
        } catch (IOException e) {
            ret = e.getMessage();
            logger.error(e.getMessage(), e);
            errorMsg = e.getMessage();
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    ret = e.getMessage();
                    logger.error(e.getMessage(), e);
                    errorMsg = e.getMessage();
                }
            }

            writeRpcLog("post", url, ret, errorMsg, sendRequestAt);

        }
        return ret;
    }

    //得到泛型类T
    public Class getGenericClass() {
        if (__klass == null) {
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                Type[] ptype = ((ParameterizedType) type).getActualTypeArguments();
                __klass = (Class) ptype[0];
            } else {
                __klass = Object.class;
            }
        }
        return __klass;
    }


    protected void writeRpcLog(String requestMethod, String url, String responseData, String exceptionMsg, Date sendRequestAt) {
        logger.info("method:[{}] url:[{}] request:[{}]  result:[] error:[{}] cose:[]", requestMethod, url, JsonUtils.toJson(getRequestParam(url)), responseData, exceptionMsg, new Date().getTime() - sendRequestAt.getTime());
    }


    private Map getRequestParam(String url) {
        Object requestData = getRequestBody();

        Map requestBody = new HashMap();

        if (!Strings.isNullOrEmpty(url)) {
            int index;
            if ((index = url.indexOf("?")) > -1) {
                String requestUrlParam = url.substring(index + 1);
                if (!Strings.isNullOrEmpty(requestUrlParam)) {
                    List<NameValuePair> nvps = URLEncodedUtils.parse(requestUrlParam, Charsets.UTF_8);
                    if (nvps != null && nvps.size() > 0) {
                        for (NameValuePair entiy : nvps) {
                            requestBody.put(entiy.getName(), entiy.getValue());
                        }
                    }

                }
            }
        }

        if (requestData != null) {
            Map map = new HashMap();
            try {
                if ((requestData instanceof String) && (Strings.isNullOrEmpty((String) requestData))) {
                } else {
                    map = JsonUtils.convert(requestData, Map.class);
                }

            } catch (Exception e) {
                map = new HashMap();
                map.put("msg", requestData);
                logger.error(e.getMessage(), e);
            }

            if (map == null || map.size() == 0) {
            } else {
                if (requestBody != null && requestBody.size() > 0) {
                    requestBody.putAll(map);
                } else {
                    requestBody = map;
                }
            }
        }

        Map requestParam = new HashMap();

        Map<String, String> requestHeader = getHeaders();
        requestHeader = requestHeader == null ? new HashMap() : requestHeader;
        requestBody = requestBody == null ? new HashMap() : requestBody;

        requestParam.put("header", requestHeader);
        requestParam.put("body", requestBody);

        return requestParam;

    }
}
