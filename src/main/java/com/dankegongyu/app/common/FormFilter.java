package com.dankegongyu.app.common;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by asheng on 2015/5/16 0016.
 */
public class FormFilter {
    private static final Logger logger = LoggerFactory.getLogger(FormFilter.class);
    private static String key = "FormFilter.getParamsData";

    private static Map<String, Object> getEnctypeParamsData(boolean filterNull) throws FileUploadException {
        Map<String, Object> returnMap = Current.get(key);
        if (returnMap != null) return returnMap;
        HttpServletRequest request = Current.getRequest();
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return getParamsData(filterNull);
        } else {
            returnMap = Maps.newHashMap();
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(50 * 1024 * 1024);
            Map<String, List<FileItem>> map = upload.parseParameterMap(request);
            if (map.keySet().size() == 0)
                return springMultipartResolver(filterNull);
            Iterator entries = map.entrySet().iterator();
            Map.Entry entry;
            String name = "";
            while (entries.hasNext()) {
                entry = (Map.Entry) entries.next();
                name = (String) entry.getKey();
                List<FileItem> list = (List<FileItem>) entry.getValue();
                if (list.size() == 1) {
                    FileItem file = list.get(0);
                    if (file.isFormField()) {
                        String value = StringUtils.trim(file.getString());
                        if (!filterNull || !Strings.isNullOrEmpty(value))
                            returnMap.put(StringUtils.trim(name), value);
                    } else {
                        returnMap.put(name, file);
                    }
//                    returnMap.put(name, file.isFormField() ? StringUtils.trim(file.getString()) : file);
                }
            }
            Current.set(key, returnMap);
            return returnMap;
        }
    }

    private static Map<String, Object> springMultipartResolver(boolean filterNull) {
        Map<String, Object> returnMap = new HashMap<>();
        MultipartHttpServletRequest request = new StandardServletMultipartResolver().resolveMultipart(Current.getRequest());
        Map properties = request.getParameterMap();


        Iterator entries = properties.entrySet().iterator();
        Map.Entry entry;
        String name = "";
        String value = "";
        while (entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            name = (String) entry.getKey();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = decode(values[i]) + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = decode(valueObj.toString());
            }
            value = StringUtils.trim(value);
            if (!filterNull || !Strings.isNullOrEmpty(value))
                returnMap.put(StringUtils.trim(name), value);
        }

        Map<String, MultipartFile> multipartFileMap = request.getFileMap();
        for (Map.Entry<String, MultipartFile> entry1 : multipartFileMap.entrySet()) {
            returnMap.put(entry1.getKey(), entry1.getValue());
        }
        if (returnMap.keySet().size() > 0)
            Current.set(Current.REQUEST, request);
        return returnMap;
    }


    private static Map<String, Object[]> springMultipartResolver() {
        Map<String, Object[]> returnMap = new HashMap<>();
        MultipartHttpServletRequest request = new StandardServletMultipartResolver().resolveMultipart(Current.getRequest());
        Map<String, String[]> params = request.getParameterMap();
        if (params != null && params.keySet().size() > 0) {
            returnMap.putAll(params);
        }
        Map<String, MultipartFile> multipartFileMap = request.getFileMap();
        for (Map.Entry<String, MultipartFile> entry1 : multipartFileMap.entrySet()) {
            returnMap.put(entry1.getKey(), new Object[]{entry1.getValue()});
        }
        if (returnMap.keySet().size() > 0)
            Current.set(Current.REQUEST, request);
        return returnMap;
    }

    private static Map<String, Object> getParamsData(boolean filterNull) throws FileUploadException {
        Map<String, Object> returnMap = Current.get(key);
        if (returnMap != null) return returnMap;

        HttpServletRequest request = Current.getRequest();
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart)
            return getEnctypeParamsData(filterNull);
        returnMap = Maps.newHashMap();
        // 参数Map
        Map properties = Current.getRequest().getParameterMap();
        // 返回值Map

        Iterator entries = properties.entrySet().iterator();
        Map.Entry entry;
        String name = "";
        String value = "";
        while (entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            name = (String) entry.getKey();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = decode(values[i]) + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = decode(valueObj.toString());
            }
            value = StringUtils.trim(value);
            if (!filterNull || !Strings.isNullOrEmpty(value))
                returnMap.put(StringUtils.trim(name), value);
        }
        Current.set(key, returnMap);
        return returnMap;

    }

    private static String decode(String str) {
        if (!Current.getRequest().getMethod().toUpperCase().equals("GET")) return str;
        if (StringUtils.isBlank(str)) return "";
        try {
            return URLDecoder.decode(str, "utf-8");
        } catch (Exception e) {
            String _str = str.replaceAll("%", "%25");
            try {
                return URLDecoder.decode(_str, "utf-8");
            } catch (UnsupportedEncodingException e1) {
                return "";
            }
        }
    }

    private static String escape(String value) {
        if (value != null) {
            value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
            value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
            value = value.replaceAll("'", "&#39;");
            value = value.replaceAll("eval\\((.*)\\)", "");
            value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
        }
        return value;
    }

    public static Map<String, Object> getParameters() {
        return getParameters(false);
    }

    public static Map<String, Object> getParametersCanJson() {
        Map<String, Object> params = getParameters();
        Map<String, Object> _data = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (!(entry.getValue() instanceof MultipartFile)) {
                _data.put(entry.getKey(), entry.getValue());
            }
        }
        return _data;
    }

    private static Map<String, Object[]> getRealParameterMap() {
        boolean isMultipart = ServletFileUpload.isMultipartContent(Current.getRequest());
        if (isMultipart) return getEnctypeParamsData();
        return getParamsData();
    }

    //        if(Current.getRequest().getContentType().equals("application/json"))

    private static Map<String, Object> getPostJson() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader((ServletInputStream) Current.getRequest().getInputStream(), "UTF-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            if (json.startsWith("["))
                json = "{\"data\":" + json + "}";
            if (!json.startsWith("{")) {
                if (json.startsWith("\""))
                    json = "{\"data\":" + json + "}";
                else
                    json = "{\"data\":\"" + json + "\"}";
            }
            return JsonUtils.convert(json, Map.class);
//            return JSON.parseObject(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Maps.newHashMap();
    }

    private static boolean isJson() {

        return Current.getRequest() != null && Current.getRequest().getContentType() != null && Current.getRequest().getContentType().indexOf("json") > 0;
    }

    private static boolean isXml() {

        return Current.getRequest() != null && Current.getRequest().getContentType() != null && Current.getRequest().getContentType().indexOf("xml") > 0;
    }

    private static Map<String, Object> getPostXml() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            InputStream in = Current.getRequest().getInputStream();
            byte[] buffer = new byte[2048];
            int length = 0;
            while ((length = in.read(buffer)) != -1) {
                bos.write(buffer, 0, length);//写入输出流
            }
            in.close();//读取完毕，关闭输入流

            String xml = new String(bos.toByteArray(), "UTF-8");
            Map<String, Object> map = new HashMap<>();
            map.put("data", xml);
            return map;
//            return JSON.parseObject(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Maps.newHashMap();
    }

    /**
     * 正常获取参数
     *
     * @return
     * @throws FileUploadException
     */
    private static Map<String, Object[]> getParamsData() {
        String key = FormFilter.class.getName() + ".getParamsData";
        if (Current.get(key) == null) {
            Map<String, Object[]> map = Maps.newHashMap();
            Map<String, String[]> properties = Current.getRequest().getParameterMap();
            Iterator entries = properties.entrySet().iterator();
            Map.Entry entry;
            String name = "";
            while (entries.hasNext()) {
                entry = (Map.Entry) entries.next();
                name = (String) entry.getKey();
                String[] val = (String[]) entry.getValue();
                if (val != null) {
                    String[] _val = new String[val.length];
                    for (int i = 0; i < val.length; i++) {
//                        _val[i] = IdDeserializer.deserialize(name, val[i]);
                        _val[i] = val[i];
                    }
                    map.put(name, _val);
                } else {
                    map.put(name, val);
                }
            }
            Current.set(key + ".old", properties);
            Current.set(key, map);
        }
        return Current.get(key);

    }

    /**
     * 获取multipart/form-data上传的参数
     *
     * @return
     * @throws FileUploadException
     */
    private static Map<String, Object[]> getEnctypeParamsData() {
        String key = FormFilter.class.getName() + ".getEnctypeParamsData";
        if (Current.get(key) == null) {
            Map<String, Object[]> returnMap = Maps.newHashMap();
            Map<String, String[]> returnMapOld = Maps.newHashMap();
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(50 * 1024 * 1024);
            Map<String, List<FileItem>> map = null;
            try {
                map = upload.parseParameterMap(Current.getRequest());
                if (map.keySet().size() == 0)
                    return springMultipartResolver();
                Iterator entries = map.entrySet().iterator();
                Map.Entry entry;
                String name = "";
                while (entries.hasNext()) {
                    entry = (Map.Entry) entries.next();
                    name = (String) entry.getKey();
                    List<FileItem> list = (List<FileItem>) entry.getValue();
                    if (list != null) {
                        List<Object> _val = Lists.newArrayList();
                        String[] _valOld = new String[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            FileItem file = list.get(i);
                            if (file.isFormField()) {
//                                _val.add(IdDeserializer.deserialize(name, file.getString("UTF-8")));
                                _val.add(file.getString("UTF-8"));
                                _valOld[i] = file.getString("UTF-8");
                                returnMapOld.put(name, _valOld);
                            } else {
                                _val.add(file);
                            }
                        }
                        returnMap.put(name, _val.toArray());
                    } else {
                        returnMap.put(name, null);
                        returnMapOld.put(name, null);
                    }
                }
                Current.set(key + ".old", returnMapOld);
                Current.set(key, returnMap);
            } catch (FileUploadException e) {
                logger.warn(e.getMessage());
                Current.set(key + ".old", returnMapOld);
                Current.set(key, returnMap);
            } catch (UnsupportedEncodingException e) {
                logger.warn(e.getMessage());
                Current.set(key + ".old", returnMapOld);
                Current.set(key, returnMap);
            }

        }
        return Current.get(key);
    }

    public static Map<String, Object> getParameters(boolean filterNull) {
        if (Current.get(key) == null) {
            Map<String, Object> returnMap = Maps.newHashMap();
            if (isJson()) {
                returnMap = getPostJson();
            } else if (isXml()) {
                returnMap = getPostXml();
            } else {
                Map<String, Object[]> properties = getRealParameterMap();
                Iterator entries = properties.entrySet().iterator();
                Map.Entry entry;
                String name = "";
                while (entries.hasNext()) {
                    entry = (Map.Entry) entries.next();
                    name = (String) entry.getKey();
                    Object[] val = (Object[]) entry.getValue();
                    if (val != null && val.length > 0) {
                        if (val[0] instanceof FileItem || val[0] instanceof MultipartFile)
                            returnMap.put(name, val[0]);
                        else {
                            String value = "";
                            for (int i = 0; i < val.length; i++) {
                                value = value + decode(val[i].toString()) + ",";
                            }
                            value = value.substring(0, value.length() - 1);
                            value = StringUtils.trim(value);
                            if (!filterNull || !Strings.isNullOrEmpty(value))
                                returnMap.put(StringUtils.trim(name), value);
                        }

                    }
                }
            }
            Current.set(key, returnMap);
        }
        return Current.get(key);

    }

    public static Object getParameter(String key) {
        return getParameters().get(key);
    }


}
