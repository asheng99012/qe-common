package com.dankegongyu.app.common.feign;

import com.dankegongyu.app.common.AppUtils;
import com.dankegongyu.app.common.CurrentContext;
import com.dankegongyu.app.common.Mock;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Base64Utils;

import java.util.Base64;

@Configuration
public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        template.header("CurrentContext", Base64Utils.encodeToString(CurrentContext.toJson().getBytes()));
//        template.uri(AppUtils.getBean(Mock.class).mockUrl(template.url()));
    }
}
