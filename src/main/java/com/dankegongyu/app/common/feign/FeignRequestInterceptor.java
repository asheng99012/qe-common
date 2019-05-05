package com.dankegongyu.app.common.feign;

import com.dankegongyu.app.common.AppUtils;
import com.dankegongyu.app.common.Current;
import com.dankegongyu.app.common.CurrentContext;
import com.dankegongyu.app.common.Mock;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Enumeration;

public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
//        HttpServletRequest request = Current.getRequest();
//        if (request == null)
//            request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        if (request != null) {
//            Enumeration enumeration = request.getHeaderNames();
//            while (enumeration.hasMoreElements()) {
//                String key = enumeration.nextElement().toString();
//                if (!template.headers().containsKey(key))
//                    template.header(key, request.getHeader(key));
//            }
//        }
        template.header(FeignFilter.key, Base64Utils.encodeToString(CurrentContext.toJson().getBytes()));
    }
}
