package com.dankegongyu.app.common.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cglib.beans.BeanMap;
import qeorm.utils.JsonUtils;

import java.io.*;
import java.util.*;

public class ExcelWriter {
    Workbook workbook = new XSSFWorkbook(); // 创建工作簿
    Sheet sheet; // 创建Sheet
    private LinkedHashMap<String, Header> headers;
    int rowIndex;

    public ExcelWriter() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("sheet0"); // 创建Sheet、
        rowIndex = 0;
    }

    public ExcelWriter setHeaders(LinkedHashMap<String, Object> header) {
        LinkedHashMap<String, Header> hs = new LinkedHashMap<>();
        header.forEach((key, val) -> {
            if (val instanceof String) {
                hs.put(key, new DefaultHeader2(val.toString()));
            } else {
                hs.put(key, (Header) val);
            }
        });
        this.headers = hs;
        List<Object> columns = new ArrayList<Object>();
        headers.forEach((key, val) -> {
            columns.add(key);
        });
        List<List> list = new ArrayList<>();
        list.add(columns);
        write(list);
        return this;
    }

    private void setAutoHeader(Object data) {
        Map map;
        if (data instanceof Map) {
            map = (Map) data;
        } else {
            map = BeanMap.create(data);
        }
        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        map.keySet().forEach((key) -> {
            header.put(String.valueOf(key), key);
        });
        setHeaders(header);
    }

    public ExcelWriter writeData(List datas) {
        if (headers == null) setAutoHeader(datas.get(0));
        List<List> list = new ArrayList<>();
        datas.forEach((obj) -> {
            List data = new ArrayList();
            headers.forEach((key, val) -> {
                data.add(headers.get(key).render(obj));
            });
            list.add(data);
        });
        write(list);
        return this;
    }

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(close().toByteArray());
    }

    public void writeTo(String path) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(fos));
        outStream.write(close().toByteArray());
        outStream.close();
    }

    private void write(List<List> dataList) {
        dataList.forEach(data -> {
            Row row = sheet.createRow(rowIndex);
            for (int i = 0; i < data.size(); i++) {
                Object item = data.get(i);
                Cell cell = row.createCell(i);
                if (item == null) {

                } else if (item instanceof Date) {
                    cell.setCellValue((Date) item);
                } else if (item instanceof Boolean) {
                    cell.setCellValue((Boolean) item);
                } else if (item instanceof Number) {
                    String val = String.valueOf(item);
                    if (val.length() > 11) {
                        cell.setCellValue(val);
                    } else {
                        cell.setCellValue(Double.parseDouble(val));
                    }
                } else if (item instanceof String) {
                    cell.setCellValue((String) item);
                } else {
                    cell.setCellValue(JsonUtils.toJson(item));
                }
            }
            rowIndex++;
        });
    }

    private ByteArrayOutputStream close() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();
        return byteArrayOutputStream;
    }
}
