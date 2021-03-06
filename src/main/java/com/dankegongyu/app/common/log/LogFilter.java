package com.dankegongyu.app.common.log;


import com.dankegongyu.app.common.*;
import com.dankegongyu.app.common.exception.BaseException;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class LogFilter implements Filter {
    Logger logger = LoggerFactory.getLogger(LogFilter.class);
    private static List<String> excludes = new ArrayList<>();

    private static boolean recordResult = false;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String exclude = filterConfig.getInitParameter("exclude");
        if (!Strings.isNullOrEmpty(exclude))
            excludes = Splitter.on(";").splitToList(exclude);
        String isrecordResult = filterConfig.getInitParameter("recordResult");
        if (!Strings.isNullOrEmpty(isrecordResult)) {
            recordResult = JsonUtils.convert(isrecordResult, Boolean.class);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        String url = Current.getRequest().getRequestURL().toString();
        if (!isNeedLog(url)) {
            chain.doFilter(request, response);
        } else {
            String type = "localLog";
            Object handler = null;
            try {
                handler = AppUtils.getBean(HandlerMapping.class).getHandler((HttpServletRequest) request).getHandler();
            } catch (Exception e) {
                chain.doFilter(request, response);
            }

//            try {
            if (handler != null && handler instanceof HandlerMethod) {
                HandlerMethod method = (HandlerMethod) handler;
                type = method.getBeanType().getName() + "." + method.getMethod().getName();
                if (recordResult)
                    logAll(request, response, chain, type);
                else
                    logRequest(request, response, chain, type);
            }

//            } catch (Exception e) {
//                throw new RuntimeException(e.getMessage(),e.getCause());
//            }

        }
    }

    public void logRequest(ServletRequest request, ServletResponse response, FilterChain chain, String type) throws IOException, ServletException {
        //todo  log
        RecordRpcLog log = (RecordRpcLog) AppUtils.getBean("localLog");
        Date start = new Date();
        try {
            chain.doFilter(request, response);
        } finally {
            if (log != null) {
                HttpServletRequest req = (HttpServletRequest) request;
                log.record(TraceIdUtils.getTraceId().split("-")[0]
                        , TraceIdUtils.getTraceId()
                        , start, new Date(), type, "", Current.getRemortIP(), req.getRequestURL().toString()
                        , req.getMethod(), null, FormFilter.getParametersCanJson(), Current.SERVERIP, true, 200, null);
            }
        }

    }

    public void logAll(ServletRequest request, ServletResponse response, FilterChain chain, String type) throws IOException, ServletException {
        ByteResponseWrapper byteResponseWrapper = new ByteResponseWrapper((HttpServletResponse) response);
        String jsonResponseString = null;
        String exceptionMsg = null;
        Date sendRequestAt = new Date();
        try {
            chain.doFilter(request, byteResponseWrapper);
            response.flushBuffer();
            jsonResponseString = new String(byteResponseWrapper.getBytes(), response.getCharacterEncoding());
            if (Strings.isNullOrEmpty(jsonResponseString) && Current.getRequest().getAttribute(BaseController.MODEL) != null) {
                jsonResponseString = JsonUtils.toJson(Current.getRequest().getAttribute(BaseController.MODEL));
            }
            if (!Strings.isNullOrEmpty(GlobalExceptionHandler.getCurrentThreadError())) {
                exceptionMsg = GlobalExceptionHandler.getCurrentThreadError();
            }

        } finally {
            try {
                RecordRpcLog log = (RecordRpcLog) AppUtils.getBean("localLog");
                if (exceptionMsg == null) exceptionMsg = getErrorMsg();
                if (log != null) {
                    HttpServletRequest req = (HttpServletRequest) request;
                    log.record(TraceIdUtils.getTraceId().split("-")[0]
                            , TraceIdUtils.getTraceId()
                            , sendRequestAt, new Date(), type, "", Current.getRemortIP(), req.getRequestURL().toString()
                            , req.getMethod(), null, FormFilter.getParametersCanJson(), Current.SERVERIP
                            , exceptionMsg != null, 200, exceptionMsg != null ? exceptionMsg : jsonResponseString);
                }

            } catch (Exception e) {
                logger.error("记录rpc日志出错:" + e.getMessage(), e);
            }

        }
    }

    @Override
    public void destroy() {

    }

    public static boolean isNeedLog(String url) {
        for (String exclude : excludes) {
            if (url.matches(exclude))
                return false;
        }
        return true;
    }

    static class ByteResponseWrapper extends HttpServletResponseWrapper {

        private ByteOutputStream output;

        public byte[] getBytes() throws IOException {
//            this.getResponse().getWriter().flush();
            return output.getBytes();
        }

        public ByteResponseWrapper(HttpServletResponse response) throws IOException {
            super(response);
            output = new ByteOutputStream();
//            output.setResponseOutputStream(this.getResponse().getOutputStream());
            output.setResponse(response);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return output;
        }
    }

    static class ByteOutputStream extends ServletOutputStream {

        private ServletOutputStream responseOutputStream;
        private ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HttpServletResponse response;

        public void setResponseOutputStream(ServletOutputStream outputStream) {
            responseOutputStream = outputStream;
        }

        public void setResponse(HttpServletResponse response) {
            this.response = response;
        }

        @Override
        public void write(int b) throws IOException {
            bos.write(b);
//            responseOutputStream.write(b);
            response.getOutputStream().write(b);
        }

        public byte[] getBytes() {
            return bos.toByteArray();
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener listener) {

        }
    }

    public static void setException(Exception e) {
        Current.set(LogFilter.class + ".error", e.getMessage() + "\r\n" + ExceptionUtils.getStackTrace(e));
    }

    public static String getErrorMsg() {
        return Current.get(LogFilter.class + ".error");
    }
}