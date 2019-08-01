package com.dankegongyu.app.common;

import com.google.common.base.Strings;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CosFilter implements Filter {
    String allowHeader = "";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String _allowHeader = filterConfig.getInitParameter("allowHeader");
        if (!Strings.isNullOrEmpty(_allowHeader))
            allowHeader = "," + _allowHeader;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        System.out.println(request.getRequestURI());
        Cookie[] cookie = request.getCookies();
        System.out.println(cookie);
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Content-Length, Authorization, Accept, X-Requested-With , x-sso-ticket,x-sso-token" + allowHeader);
        response.setHeader("Access-Control-Allow-Credentials", "true");

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
