package com.dankegongyu.app.common.feign;

import com.dankegongyu.app.common.AppUtils;
import com.dankegongyu.app.common.Current;
import com.dankegongyu.app.common.JsonUtils;
import com.dankegongyu.app.common.TraceIdUtils;
import com.dankegongyu.app.common.log.RecordRpcLog;
import feign.Client;
import feign.Request;
import feign.Response;
import feign.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.core.NamedThreadLocal;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static feign.Util.*;
import static java.lang.String.format;

public class DKLoadBalancerFeignClient extends LoadBalancerFeignClient {

    public DKLoadBalancerFeignClient(Client delegate, CachingSpringLoadBalancerFactory lbClientFactory, SpringClientFactory clientFactory) {
        super(delegate, lbClientFactory, clientFactory);
        replaceClient(delegate);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        if (!DefaultClient.getSource().containsKey("oriUrl")) {
            DefaultClient.getSource().put("oriUrl", request.url());
            if (!DefaultClient.getSource().containsKey("type")) {
                try {
                    DefaultClient.getSource().put("type", new URI(request.url()).getPath().replace("//commrpc/", "").replace("/commrpc/", "").replace("/", "."));
                } catch (URISyntaxException e) {
                }
            }
        }
        if ((Boolean) DefaultClient.getSource().get("redirect")) {
            return ((LoadBalancerFeignClient) this.getDelegate()).getDelegate().execute(request, options);
        }
        return this.getDelegate().execute(request, options);
    }

    public void replaceClient(Client client) {
        if (client instanceof LoadBalancerFeignClient) {
            replaceClient(((LoadBalancerFeignClient) client).getDelegate());
        } else {
            Field field = null;
            try {
                field = client.getClass().getDeclaredField("delegate");
            } catch (NoSuchFieldException e) {
                try {
                    field = client.getClass().getField("delegate");
                } catch (NoSuchFieldException e1) {

                }
            }
            if (field != null) {
                field.setAccessible(true);
                try {
                    Client _c = (Client) field.get(client);
                    if (_c.getClass().getName().equals(Client.Default.class.getName())) {
                        field.set(client, new DefaultClient());
                    } else {
                        replaceClient(_c);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static class FeiginProxy implements MethodInterceptor {

        public Proxy proxy;
        public Target.HardCodedTarget target;

        public static Object proxy(Proxy proxy) {
            Object object = Proxy.getInvocationHandler(proxy);
            try {
                FeiginProxy feiginProxy = new FeiginProxy();
                feiginProxy.proxy = proxy;
                Field field = object.getClass().getDeclaredField("target");
                field.setAccessible(true);
                feiginProxy.target = (Target.HardCodedTarget) field.get(object);
                Enhancer enhancer = new Enhancer();
                enhancer.setInterfaces(new Class[]{feiginProxy.target.type()});
                enhancer.setCallback(feiginProxy);
                return enhancer.create();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            DefaultClient.clear();
            if (!ICommRpc.class.isAssignableFrom(target.type()))
                DefaultClient.getSource().put("type", target.type().getName() + "." + method.getName());
            DefaultClient.getSource().put("redirect", !target.url().contains(target.name()));
            try {
                return method.invoke(proxy, objects);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

    public static class DefaultClient implements Client {
        Logger logger = LoggerFactory.getLogger(DefaultClient.class);
        private final static ThreadLocal<Map<String, Object>> resources = new NamedThreadLocal<Map<String, Object>>(DefaultClient.class.getName());
        private final SSLSocketFactory sslContextFactory;
        private final HostnameVerifier hostnameVerifier;

        public DefaultClient() {
            sslContextFactory = null;
            hostnameVerifier = null;
        }

        /**
         * Null parameters imply platform defaults.
         */
        public DefaultClient(SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier) {
            this.sslContextFactory = sslContextFactory;
            this.hostnameVerifier = hostnameVerifier;
        }

        @Override
        public Response execute(Request request, Request.Options options) throws IOException {
            getSource().put("start", new Date());
            Response response = null;
            try {
                HttpURLConnection connection = convertAndSend(request, options);
                response = convertResponse(connection, request);
            } catch (Exception e) {
                getSource().put("body", e.getMessage());
                throw e;
            }
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

        public static Map<String, Object> getSource() {
            Map<String, Object> map = resources.get();
            if (map == null) {
                map = new HashMap<String, Object>();
                resources.set(map);
            }
            return map;
        }

        public static void clear() {
            resources.remove();
        }

        public void log(Request request, Response response) {
            try {
                RecordRpcLog log = (RecordRpcLog) AppUtils.getBean("rpcLog");
                if (log != null) {
                    String url = request.url();
                    URI uri = new URI(url);
                    Map reqp = new HashMap();
                    try {
                        reqp = JsonUtils.convert(request.requestBody().asString(), Map.class);
                    } catch (Exception e) {
                        try {
                            String data = request.requestBody().asString();
                            if (!data.equals("Binary data"))
                                reqp.put("data", data);

                        } catch (Exception ex) {
//                            reqp.put("data", ex.getMessage());
                        }
                    }
                    log.record(TraceIdUtils.getTraceId().split("-")[0]
                            , TraceIdUtils.getTraceId()
                            , (Date) getSource().get("start"), new Date(), getSource().get("type").toString(), "", Current.SERVERIP
                            , url, request.httpMethod().name(), request.headers()
                            , reqp, uri.getHost()
                            , response != null && response.status() == 200, response != null ? response.status() : 0, getSource().get("body").toString());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}