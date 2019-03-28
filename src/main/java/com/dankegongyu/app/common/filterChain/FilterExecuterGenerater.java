package com.dankegongyu.app.common.filterChain;

import com.alibaba.fastjson.JSON;
import com.dankegongyu.app.common.AppUtils;
import com.dankegongyu.app.common.JsonUtils;
import com.dankegongyu.app.common.Wrap;
import com.dankegongyu.app.common.exception.NeedEmailException;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterExecuterGenerater {
    Logger logger = LoggerFactory.getLogger(FilterExecuterGenerater.class);
    public Map<String, Filter> config;
    Map<String, Object> originData;
    String basePackage;
    int depth = 1;
    String jsonString;
    Class<? extends FilterExecuter> executerClass;
    boolean containChild;

    public static Map<String, Filter> generate(String jsonString, int depth) {
        return generate(jsonString, depth, null, FilterExecuter.class, false);
    }

    public static Map<String, Filter> generate(String jsonString, int depth, String basePackage) {
        return generate(jsonString, depth, basePackage, FilterExecuter.class, false);
    }

    public static Map<String, Filter> generate(String jsonString, int depth, String basePackage, Class<? extends FilterExecuter> executerClass) {
        return (new FilterExecuterGenerater(jsonString, depth, basePackage, executerClass, true)).config;
    }

    public static Map<String, Filter> generate(String jsonString, int depth, String basePackage, Class<? extends FilterExecuter> executerClass, boolean containChild) {
        return (new FilterExecuterGenerater(jsonString, depth, basePackage, executerClass, containChild)).config;
    }

    private FilterExecuterGenerater(String jsonString, int depth, String basePackage, Class<? extends FilterExecuter> executerClass, boolean containChild) {
        config = new HashMap<>();
        this.jsonString = jsonString;
        this.depth = depth - 1;
        this.basePackage = basePackage;
        this.executerClass = executerClass;
        this.containChild = containChild;
        this.init();
    }

    public void init() {
        originData = JSON.parseObject(jsonString);
        parse("", 0);
    }

    public void parse(String path, int curDepth) {
        Map<String, Object> data = originData;
        if (curDepth > 0) {
            data = fetchVal(path);
        }
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = path + (path.equals("") ? "" : ".") + entry.getKey();
            if (curDepth < depth) {
                parse(key, curDepth + 1);
            } else {
                Object val = fetchVal(key);
                if (val instanceof List)
                    createFilter(key, fetchVal(key));
            }
        }
    }


    private Filter createFilter(String name, List val) {
        logger.info(name);
        name = name.trim();
        if (config.containsKey(name)) {
            return config.get(name);
        }
        List<Filter> list = toListFilter(val);
        FilterExecuter filter = createFilterExecuter();
        filter.init(name, list);
        config.put(name, filter);
        return filter;
    }

    private Filter createFilter(Map<String, Object> val) {
        try {
            FilterExecuter executer = (FilterExecuter) Class.forName(createKlassName(val.get("executer").toString())).newInstance();
            Object item = val.get("item");
            String name = JsonUtils.toJson(val);
            List<Filter> list;
            if (item instanceof String) {
                list = new ArrayList<>();
                Splitter.on("&&").splitToList((String) item).forEach(v -> {
                    list.add(toFilterExecuter(v));
                });
                executer.init(name, list, false);
            } else {
                list = toListFilter(JsonUtils.convert(item, List.class));
                executer.init(name, list);
            }
            return executer;
        } catch (Exception e) {
            throw new NeedEmailException(e.getMessage(), e.getCause());
        }
    }

    private String createKlassName(String val) {
        if (Strings.isNullOrEmpty(basePackage))
            return val;
        else
            return basePackage + "." + val;
    }

    private Filter createFilter(String val) {
        logger.info(val);
        if (config.containsKey(val)) {
            return config.get(val);
        }
        Filter filter;
        if (val.contains("&&")) {
            List<Filter> list = new ArrayList<>();
            Splitter.on("&&").splitToList(val).forEach(v -> {
                list.add(toFilterExecuter(v));
            });
            filter = createFilterExecuter();
            ((FilterExecuter) filter).init(val, list, false);
        } else {
            filter = createFilterByName(val);
        }
        if (containChild) config.put(val, filter);
        return filter;
    }

    private Filter createFilterByName(String filterName) {
        try {
            logger.info(filterName);
            Class klass = Class.forName(createKlassName(filterName));
            Filter filter = (Filter) AppUtils.getBean(klass);
            return filter;
        } catch (ClassNotFoundException e) {
            throw new NeedEmailException(e.getMessage(), e.getCause());
        }
    }

    <T> T fetchVal(String path) {
        return (T) Wrap.getWrap(originData).getValue(path);
    }

    void dealList(List listItem, Object val) {
        if (val instanceof String) {
            String type = ((String) val).trim();
            if (fetchVal(type) == null) {
                listItem.add(type);
            } else {
                listItem.addAll(fetchVal(type));
            }
        } else {
            listItem.add(val);
        }
    }

    List<Filter> toListFilter(List val) {
        List listItem = new ArrayList();
        val.forEach(v -> {
            dealList(listItem, v);
        });

        List<Filter> list = new ArrayList<>();
        listItem.forEach(v -> {
            if (v instanceof String)
                list.add(createFilter((String) v));
            else if (v instanceof Map)
                list.add(createFilter((Map<String, Object>) v));
        });
        return list;
    }

    FilterExecuter toFilterExecuter(String val) {
        val = val.trim();
        List listItem = new ArrayList();
        dealList(listItem, val);
        List<Filter> list = toListFilter(listItem);
        FilterExecuter filter = createFilterExecuter();
        filter.init(val, list);
        if (containChild) config.put(val, filter);
        return filter;
    }

    FilterExecuter createFilterExecuter() {
        try {
            return executerClass.newInstance();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new NeedEmailException(e.getMessage(), e);
        }
    }

}
