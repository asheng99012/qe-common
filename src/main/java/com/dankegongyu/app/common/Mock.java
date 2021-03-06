package com.dankegongyu.app.common;

import com.google.common.base.Strings;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

public class Mock {
    private String proxyHost;
    private List<String> mockUrlList;

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public List<String> getMockUrlList() {
        return mockUrlList;
    }

    public void setMockUrlList(List<String> mockUrlList) {
        this.mockUrlList = mockUrlList;
    }

    public void addUrl(String url) {
        if (mockUrlList.indexOf(url) == -1)
            mockUrlList.add(url);
    }

    public void removeUrl(String url) {
        if (mockUrlList.indexOf(url) > -1) {
            mockUrlList.remove(url);
        }
    }

    //是否需要对 url mock
    public boolean isMock(String url) {
        if (Strings.isNullOrEmpty(proxyHost)) return false;
        for (int i = 0; i < mockUrlList.size(); i++) {
            if (url.indexOf(mockUrlList.get(i)) == 0 || url.matches(mockUrlList.get(i))) {
                return true;
            }
        }
        return false;
    }

    public String mockUrl(String url) {
        if (isMock(url))
            url = url.replaceAll(":\\d+", "").replaceAll("^https?://", proxyHost);
        return url;
    }

    public static CloseableHttpClient proxy(CloseableHttpClient client) {
        return Proxy.create(client);
    }

    public static class Proxy implements MethodInterceptor {
        CloseableHttpClient client;

        public static CloseableHttpClient create(CloseableHttpClient client) {
            Proxy proxy = new Proxy();
            proxy.client = client;
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(CloseableHttpClient.class);
            enhancer.setCallback(proxy);
            enhancer.setClassLoader(CloseableHttpClient.class.getClassLoader());
            return (CloseableHttpClient) enhancer.create();
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            if (method.getName().equals("execute") && objects.length == 1 && objects[0] instanceof HttpRequestBase) {
                HttpRequestBase request = (HttpRequestBase) objects[0];
                if(AppUtils.getBean(Mock.class)!=null) {
                    request.setURI(new URI(AppUtils.getBean(Mock.class).mockUrl(request.getURI().toString())));
                }
            }
            return methodProxy.invoke(this.client, objects);
        }
    }
}
