package com.dankegongyu.app.common.excel;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.dankegongyu.app.common.CsvUtils;

import java.io.*;
import java.util.*;

public class Writer {
    private LinkedHashMap<String, Header> headers;
    private List datas;

    public Writer(LinkedHashMap<String, Object> header, List datas) {
        this.datas = datas;
        LinkedHashMap<String, Header> hs = new LinkedHashMap<>();
        header.forEach((key, val) -> {
            if (val instanceof String) {
                hs.put(key, new DefaultHeader(val.toString()));
            } else {
                hs.put(key, (Header) val);
            }
        });
        this.headers = hs;
    }

    public List<List<String>> getDatas() {
        List<List<String>> list = new ArrayList<>();
        List<String> columns = new ArrayList<String>();
        headers.forEach((key, val) -> {
            columns.add(key);
        });
        list.add(columns);

        datas.forEach((obj) -> {
            List<String> data = new ArrayList<String>();
            columns.forEach((key) -> {
                data.add(headers.get(key).render(obj));
            });
            list.add(data);
        });

        return list;
    }

    public InputStream getInputStream() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeTo(out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    public void writeTo(OutputStream out) {
        ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, false);
        //写第一个sheet, sheet1  数据全是List<String> 无模型映射关系
        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName("sheet0");
        writer.write0(getDatas(), sheet1);
        writer.finish();
    }
}

