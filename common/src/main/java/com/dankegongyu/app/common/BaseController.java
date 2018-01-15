package com.dankegongyu.app.common;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    public static <T> T getParameter(String key) {
        Object ret = getParameters().get(key);
        if (ret != null) return (T) ret;
        return null;
    }


    @RequestMapping(value = "/{page}")
    public String simplePage(@PathVariable("page") String page) throws Exception {
        logger.info(this.getClass().getName() + " simplePage");
        if (Current.isAjax()) return toJson(new ApiResult("找不到" + Current.getRequest().getRequestURI()));
        return toView(new Object());
    }

    //各个静态页面，如：绑定用户页面、登录页面、注册页面
    @RequestMapping(value = "/{target}/{action}")
    public String simplePage(@PathVariable("target") String target, @PathVariable("action") String action) throws Exception {
        logger.info(this.getClass().getName() + " simplePage");
        if (Current.isAjax()) return toJson(new ApiResult("找不到" + Current.getRequest().getRequestURI()));
        return toView("/" + target + "/" + action, new Object());
    }

    @RequestMapping(value = "/{target}/{sub}/{action}")
    public String simplePage(@PathVariable("target") String target, @PathVariable("sub") String sub, @PathVariable("action") String action) throws Exception {
        logger.info(this.getClass().getName() + " simplePage");
        if (Current.isAjax()) return toJson(new ApiResult("找不到" + Current.getRequest().getRequestURI()));
        return toView("/" + target + "/" + sub + "/" + action, new Object());
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
    public String toCsv(List list, String[] column, String[] chineses) throws IOException {
        HttpServletResponse response = Current.getResponse();
        response.setContentType("application/octet-stream");
//        response.setHeader("Content-type","text/csv");
        response.setHeader("Content-Disposition", "attachment;filename=" + System.currentTimeMillis() + ".csv");
        response.setHeader("Cache-Control", "must-revalidate,post-check=0,pre-check=0");
        response.setHeader("'Expires", "0");
        response.setHeader("Pragma", "public");
        byte commonCsvHead[] = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte csv[] = CsvUtils.formatToCsv(list, column, chineses).getBytes("UTF-8");
        byte data[] = new byte[commonCsvHead.length + csv.length];
        System.arraycopy(commonCsvHead, 0, data, 0, commonCsvHead.length);
        System.arraycopy(csv, 0, data, commonCsvHead.length, csv.length);
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
        // response.getOutputStream().close();
        return null;
    }

    /**
     * @param list
     * @param column 全部字段默认null
     * @return
     * @throws IOException
     */
    public String toCsvWithRelation(List list, String[] column, String[] chineses) throws IOException {
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
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
        // response.getOutputStream().close();
        return null;
    }

    public String toCsv(List list) throws IOException {
        return toCsv(list, null, null);
    }

    public String toCsv(List list, String[] column) throws IOException {
        return toCsv(list, column, null);
    }

    public String toJson(Object model) {
        if (!(model instanceof ApiResult)) model = new ApiResult(model);
        BaseController.writeJsonToClient(model);
        Current.set("tojson", "tojson");
        return "page/json";
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

        return "page/" + viewPath;
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
