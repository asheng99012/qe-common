package com.dankegongyu.app.common.feign;

import com.dankegongyu.app.common.*;
import com.dankegongyu.app.common.log.RecordRpcLog;
import feign.Client;
import feign.Request;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.FeignLoadBalancer;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.NamedThreadLocal;
import org.springframework.util.Base64Utils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static feign.Util.*;
import static java.lang.String.format;

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


    public static class ClientFilter implements Client {
        Logger logger = LoggerFactory.getLogger(ClientFilter.class);
        private final ThreadLocal<Map<String, Object>> resources = new NamedThreadLocal<Map<String, Object>>(ClientFilter.class.getName());
        private final SSLSocketFactory sslContextFactory;
        private final HostnameVerifier hostnameVerifier;

        /**
         * Null parameters imply platform defaults.
         */
        public ClientFilter(SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier) {
            this.sslContextFactory = sslContextFactory;
            this.hostnameVerifier = hostnameVerifier;
        }

        @Override
        public Response execute(Request request, Request.Options options) throws IOException {
            resources.remove();
            getSource().put("start", new Date());
            HttpURLConnection connection = convertAndSend(request, options);
            Response response = convertResponse(connection, request);
            log(request, response);
            return response;
        }

        HttpURLConnection convertAndSend(Request request, Request.Options options) throws IOException {
            final HttpURLConnection connection =
                    (HttpURLConnection) new URL(request.url()).openConnection();
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection sslCon = (HttpsURLConnection) connection;
                if (sslContextFactory != null) {
                    sslCon.setSSLSocketFactory(sslContextFactory);
                }
                if (hostnameVerifier != null) {
                    sslCon.setHostnameVerifier(hostnameVerifier);
                }
            }
            connection.setConnectTimeout(options.connectTimeoutMillis());
            connection.setReadTimeout(options.readTimeoutMillis());
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(options.isFollowRedirects());
            connection.setRequestMethod(request.httpMethod().name());

            Collection<String> contentEncodingValues = request.headers().get(CONTENT_ENCODING);
            boolean gzipEncodedRequest =
                    contentEncodingValues != null && contentEncodingValues.contains(ENCODING_GZIP);
            boolean deflateEncodedRequest =
                    contentEncodingValues != null && contentEncodingValues.contains(ENCODING_DEFLATE);

            boolean hasAcceptHeader = false;
            Integer contentLength = null;
            for (String field : request.headers().keySet()) {
                if (field.equalsIgnoreCase("Accept")) {
                    hasAcceptHeader = true;
                }
                for (String value : request.headers().get(field)) {
                    if (field.equals(CONTENT_LENGTH)) {
                        if (!gzipEncodedRequest && !deflateEncodedRequest) {
                            contentLength = Integer.valueOf(value);
                            connection.addRequestProperty(field, value);
                        }
                    } else {
                        connection.addRequestProperty(field, value);
                    }
                }
            }
            // Some servers choke on the default accept string.
            if (!hasAcceptHeader) {
                connection.addRequestProperty("Accept", "*/*");
            }

            if (request.body() != null) {
                if (contentLength != null) {
                    connection.setFixedLengthStreamingMode(contentLength);
                } else {
                    connection.setChunkedStreamingMode(8196);
                }
                connection.setDoOutput(true);
                OutputStream out = connection.getOutputStream();
                if (gzipEncodedRequest) {
                    out = new GZIPOutputStream(out);
                } else if (deflateEncodedRequest) {
                    out = new DeflaterOutputStream(out);
                }
                try {
                    out.write(request.body());
                } finally {
                    try {
                        out.close();
                    } catch (IOException suppressed) { // NOPMD
                    }
                }
            }
            return connection;
        }

        Response convertResponse(HttpURLConnection connection, Request request) throws IOException {
            int status = connection.getResponseCode();
            String reason = connection.getResponseMessage();

            if (status < 0) {
                throw new IOException(format("Invalid status(%s) executing %s %s", status,
                        connection.getRequestMethod(), connection.getURL()));
            }

            Map<String, Collection<String>> headers = new LinkedHashMap<String, Collection<String>>();
            for (Map.Entry<String, List<String>> field : connection.getHeaderFields().entrySet()) {
                // response message
                if (field.getKey() != null) {
                    headers.put(field.getKey(), field.getValue());
                }
            }

            Integer length = connection.getContentLength();
            if (length == -1) {
                length = null;
            }
            InputStream stream;
            if (status >= 400) {
                stream = connection.getErrorStream();
            } else {
                stream = connection.getInputStream();
            }
            getSource().put("body", inputStream2String(stream));
            stream.close();
            stream = string2InputStream(getSource().get("body").toString());
            return Response.builder()
                    .status(status)
                    .reason(reason)
                    .headers(headers)
                    .request(request)
                    .body(stream, length)
                    .build();
        }

        String inputStream2String(InputStream inputStream) {
            try {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                String str = result.toString(StandardCharsets.UTF_8.name());
                return str;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
        }

        InputStream string2InputStream(String str) {
            ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
            return stream;
        }

        public Map<String, Object> getSource() {
            Map<String, Object> map = resources.get();
            if (map == null) {
                map = new HashMap<String, Object>();
                resources.set(map);
            }
            return map;
        }

        public void log(Request request, Response response) {
            try {
                RecordRpcLog log = (RecordRpcLog) AppUtils.getBean("rpcLog");
                if (log != null) {
                    String url = request.url();
                    URI uri = new URI(url);
                    log.record(TraceIdUtils.getTraceId().split("-")[0]
                            , TraceIdUtils.getTraceId()
                            , (Date) getSource().get("start"), new Date(), "rpcLog", "", Current.SERVERIP
                            , uri.getHost() + ":" + uri.getPort(), url
                            , request.httpMethod().name(), request.headers()
                            , JsonUtils.convert(request.requestBody().asString(), Map.class), uri.getHost()
                            , response.status() == 200, response.status(), getSource().get("body").toString());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
