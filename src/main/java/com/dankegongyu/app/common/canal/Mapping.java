package com.dankegongyu.app.common.canal;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class Mapping {
    static Logger logger = LoggerFactory.getLogger(Mapping.class);
    private static Map<String, Converter> mysqlTypeMapping;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        mysqlTypeMapping = Maps.newHashMap();
        mysqlTypeMapping.put("char", data -> data);
        mysqlTypeMapping.put("text", data -> data);
        mysqlTypeMapping.put("blob", data -> data);
        mysqlTypeMapping.put("int", Long::valueOf);
        mysqlTypeMapping.put("date", Mapping::toDate);
        mysqlTypeMapping.put("time", Mapping::toDate);
        mysqlTypeMapping.put("float", Double::valueOf);
        mysqlTypeMapping.put("double", Double::valueOf);
        mysqlTypeMapping.put("decimal", Double::valueOf);
    }

    static Date toDate(String data) {
        if (data.indexOf(" ") == -1) {
            data = data + " 00:00:00";
        }
        try {
            return simpleDateFormat.parse(data);
        } catch (Exception e) {
            logger.error("{} error:{}", data, e.getMessage(), e);
            try {
                return simpleDateFormat.parse(data);
            } catch (Exception ex) {
                logger.error("{} error:{}", data, ex.getMessage(), ex);
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
        }
    }

    public static Object converter(String mysqlType, String data) {
        Optional<Map.Entry<String, Converter>> result = mysqlTypeMapping.entrySet().parallelStream().filter(entry -> mysqlType.toLowerCase().contains(entry.getKey())).findFirst();
        return (result.isPresent() ? result.get().getValue() : (Converter) data1 -> data1).convert(data);
    }

    private interface Converter {
        Object convert(String data);
    }
}
