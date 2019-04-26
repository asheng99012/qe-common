package com.dankegongyu.app.common.feign;

import com.dankegongyu.app.common.CurrentContext;
import com.google.common.base.Strings;
import org.springframework.util.Base64Utils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(filterName = "current", urlPatterns = {"/*"})
public class FeignFilter implements Filter {
    public static String key="CurrentContext";
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String json = req.getHeader("CurrentContext");
        if (!Strings.isNullOrEmpty(json)) {
            CurrentContext.resetFromJson(new String(Base64Utils.decodeFromString(json)));
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
