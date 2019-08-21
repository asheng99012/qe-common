package com.dankegongyu.app.common;

import com.dankegongyu.app.common.exception.BusinessException;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InternalFilter implements Filter {
    String notAllowDomain;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        notAllowDomain = filterConfig.getInitParameter("notAllowDomain");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        boolean _isInternalDomain = isInternalDomain(Current.getRequest().getServerName());
        if (_isInternalDomain) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            throw new BusinessException("内部接口，不允许通过" + Current.getRequest().getServerName() + "访问");
        }
    }

    @Override
    public void destroy() {

    }

    public boolean isInternalDomain(String domain) {
        if (domain.matches(notAllowDomain))
            return false;
        return true;
    }

}
