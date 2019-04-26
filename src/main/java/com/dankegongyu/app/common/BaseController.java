package com.dankegongyu.app.common;

import com.google.common.base.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by asheng on 2015/5/29 0029.
 */
public class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    public static String MODEL = "model";

    public static Map<String, Object> getParameters() {
        return FormFilter.getParameters();
    }

    public <T> T getModelFromParameter(Class<T> klass) {
        Map<String, Object> map = FormFilter.getParameters(true);
        return JsonUtils.convert(map, klass);
    }

    public <T> T getModelFromParameter(Class<T> klass,boolean filterBlank) {
        Map<String, Object> map = FormFilter.getParameters(true);
        Map<String, Object> data = new HashMap<>();
        map.forEach((key, val) -> {
            if (!(val instanceof String && val.toString().equals("")))
                data.put(key, val);
        });
        return JsonUtils.convert(data, klass);
    }

    public static <T> T getParameter(String key) {
        Object ret = getParameters().get(key);
        if (ret != null) return (T) ret;
        return null;
    }


    public String forward(String path) {
        return "forward:" + getProjectPath(path);
    }

    public String redirect(String path) {
        return "redirect:" + getProjectPath(path);
    }

    private String getProjectPath(String path) {
        if (path.substring(0, 1).equals("/") && Current.getRequest() != null)
            return Current.getRequest().getContextPath() + path;
        return path;
    }

    /**
     * @param list
     * @param column 全部字段默认null
     * @return
     * @throws IOException
     */
    public String toCsv(List list, String[] column, String[] chineses)throws IOException{
       return toCsv(list,column,chineses,"");
    }
    public String toCsv(List list, String[] column, String[] chineses,String title) throws IOException {
        HttpServletResponse response = Current.getResponse();
        response.setContentType("application/octet-stream");
//        response.setHeader("Content-type","text/csv");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(String.valueOf(title), "UTF-8")+"_"+System.currentTimeMillis() + ".csv");
        response.setHeader("Cache-Control", "must-revalidate,post-check=0,pre-check=0");
        response.setHeader("'Expires", "0");
        response.setHeader("Pragma", "public");
        byte commonCsvHead[] = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte csv[] = CsvUtils.formatToCsv(list, column, chineses).getBytes("UTF-8");
        byte data[] = new byte[commonCsvHead.length + csv.length];
        System.arraycopy(commonCsvHead, 0, data, 0, commonCsvHead.length);
        System.arraycopy(csv, 0, data, commonCsvHead.length, csv.length);
        Current.getRequest().setAttribute(MODEL, new String(data, Charsets.UTF_8));
        return "json";
    }

    /**
     * @param list
     * @param column 全部字段默认null
     * @return
     * @throws IOException
     */

    public String toCsvWithRelation(List list, String[] column, String[] chineses,String title) throws IOException {
        HttpServletResponse response = Current.getResponse();
        response.setContentType("application/octet-stream");
//        response.setHeader("Content-type","text/csv");
        response.setHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".csv");
        response.setHeader("Cache-Control", "must-revalidate,post-check=0,pre-check=0");
        response.setHeader("'Expires", "0");
        response.setHeader("Pragma", "public");
        byte commonCsvHead[] = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte csv[] = CsvUtils.formatToCsvWithRelation(list, column, chineses).getBytes("UTF-8");
        byte data[] = new byte[commonCsvHead.length + csv.length];
        System.arraycopy(commonCsvHead, 0, data, 0, commonCsvHead.length);
        System.arraycopy(csv, 0, data, commonCsvHead.length, csv.length);
//        response.getOutputStream().write(data);
//        response.getOutputStream().flush();
        // response.getOutputStream().close();
        Current.getRequest().setAttribute(MODEL, new String(data, Charsets.UTF_8));
        return "json";
    }

    public String toCsv(List list) throws IOException {
        return toCsv(list, null, null);
    }
    public String toCsv(List list,String title) throws IOException {
        return toCsv(list, null, null,title);
    }
    public String toCsv(List list, String[] column) throws IOException {
        return toCsv(list, column, null);
    }

    public String toJson(Object model) {
        if (!(model instanceof ApiResult)) model = new ApiResult(model);
        BaseController.writeJsonToClient(model);
        Current.set("tojson", "tojson");
        return "json";
    }


    public String toJson() {
        return toJson(new ApiResult());
    }

    public String toView(String viewPath, Object model) {

        if (Current.isAjax()) {
            return toJson(model);
        }

        if (!(model instanceof ApiResult)) {
            model = new ApiResult(model);
        }

        Current.getRequest().setAttribute(MODEL, model);

        return  viewPath;
    }

    public String toView(Object model) {
        return toView(Current.getRequest().getServletPath(), model);
    }

    public String toView() {
        return toView(null);
    }

    public static void writeJsonToClient(Object model) {
        if (!(model instanceof ApiResult)) model = new ApiResult(model);
        HttpServletResponse response = Current.getResponse();
        if (response == null) return;
        response.setContentType("application/json;charset=UTF-8");
        String json = JsonUtils.toJson(model);
        String cb = getParameter("callback");
        if (StringUtils.isNotEmpty(cb)) json = cb + "(" + json + ")";
        Current.getRequest().setAttribute(MODEL, json);
        if (Current.get("---isunit---") != null)
            Current.getRequest().setAttribute(MODEL, model);
    }


}
