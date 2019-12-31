package com.dankegongyu.app.common.excel;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.springframework.cglib.beans.BeanMap;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XWriter {
    private OutputStream out;
    private ExcelWriter writer;
    Sheet sheet;
    private LinkedHashMap<String, Header> headers;
    private boolean isClose = false;
    private String path;

    public XWriter(String path) {
        try {
            this.path = path;
            out = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        init();
    }

    public XWriter() {
        out = new ByteArrayOutputStream();
        init();
    }

    public void init() {
        writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, false);
        sheet = new Sheet(1, 0);
        sheet.setSheetName("sheet0");
    }

    public XWriter setHeaders(LinkedHashMap<String, Object> header) {
        LinkedHashMap<String, Header> hs = new LinkedHashMap<>();
        header.forEach((key, val) -> {
            if (val instanceof String) {
                hs.put(key, new DefaultHeader(val.toString()));
            } else {
                hs.put(key, (Header) val);
            }
        });
        this.headers = hs;
        List<List<String>> list = new ArrayList<>();
        List<String> columns = new ArrayList<String>();
        headers.forEach((key, val) -> {
            columns.add(key);
        });
        list.add(columns);
        writer.write0(list, sheet);
        return this;
    }

    public void setAutoHeader(Object data) {
        Map map;
        if (data instanceof Map) {
            map = (Map) data;
        } else {
            map = BeanMap.create(data);
        }
        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        map.keySet().forEach((key) -> {
            header.put(String.valueOf(key), "{" + key + "}");
        });
        setHeaders(header);
    }

    public XWriter writeData(List datas) {
        if (headers == null) setAutoHeader(datas.get(0));
        List<List<String>> list = new ArrayList<>();
        datas.forEach((obj) -> {
            List<String> data = new ArrayList<String>();
            headers.forEach((key, val) -> {
                data.add(headers.get(key).render(obj));
            });
            list.add(data);
        });
        writer.write0(list, sheet);
        return this;
    }

    public InputStream getInputStream() {
        if (out instanceof ByteArrayOutputStream) {
            return new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray());
        }
        if (out instanceof FileOutputStream) {
            close();
            try {
                return new FileInputStream(new File(path));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
        }
        return null;
    }

    public void close() {
        if (isClose) return;
        writer.finish();
        try {
            if (out != null)
                out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isClose = true;
    }

}

