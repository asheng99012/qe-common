package com.dankegongyu.app.common.excel;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class MoreSheetWriter {
    public OutputStream out;
    public ExcelWriter writer;
    public int index = 0;

    public MoreSheetWriter(String path) {
        try {
            out = new FileOutputStream(path);
            writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public MoreSheetWriter(OutputStream out) {
        writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, false);
    }

    public void write(String sheetName, List<List<String>> data) {
        index++;
        Sheet sheet1 = new Sheet(index, 0);
        sheet1.setSheetName(sheetName);
        writer.write0(data, sheet1);
    }

    public void close() {
        writer.finish();
        try {
            if (out != null)
                out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
