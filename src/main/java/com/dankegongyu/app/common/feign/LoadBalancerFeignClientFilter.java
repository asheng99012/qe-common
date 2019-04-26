package com.dankegongyu.app.common.feign;

import com.dankegongyu.app.common.AppUtils;
import com.dankegongyu.app.common.CurrentContext;
import com.dankegongyu.app.common.Mock;
import feign.Client;
import feign.Request;
import feign.Response;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.FeignLoadBalancer;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LoadBalancerFeignClientFilter extends LoadBalancerFeignClient {
    public LoadBalancerFeignClientFilter(Client delegate, CachingSpringLoadBalancerFactory lbClientFactory, SpringClientFactory clientFactory) {
        super(delegate, lbClientFactory, clientFactory);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        String ori_url = request.url();
        String url = ori_url;
        if (AppUtils.getBean(Mock.class) != null)
            url = AppUtils.getBean(Mock.class).mockUrl(request.url());
//重新构建 request　对象
        Request newRequest = Request.create(request.httpMethod(), url, request.headers(), request.requestBody());
        if (ori_url.equals(url))
            return super.execute(newRequest, options);
        else return super.getDelegate().execute(newRequest, options);
    }

}
