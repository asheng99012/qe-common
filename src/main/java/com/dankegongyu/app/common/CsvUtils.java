package com.dankegongyu.app.common;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.beans.BeanMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by asheng on 2015/7/9 0009.
 */
public class CsvUtils {
    public static String formatToCsv(List data) {
        return formatToCsv(data, null, null);
    }

    public static String formatToCsv(List data, String[] column) {
        return formatToCsv(data, column, null);
    }

    public static String formatToCsv(List data, String[] column, String[] chineses) {
        if (column == null && data.size() > 0) {
            Map<String, Object> map;
            if(data.get(0) instanceof Map)
                map=(Map<String, Object>)data.get(0);
            else
                map= BeanMap.create(data.get(0));
            column=new String[map.keySet().size()];
            int i=0;
            for(String c:map.keySet()){
                column[i]=c;
                i++;
            }
        }
        if (chineses == null) chineses = column;
        List<String> list = new ArrayList<String>();
        List<String> keys = new ArrayList<String>();
        for (int i = 0; i < column.length; i++) {
            int index = null != chineses && chineses.length > 0 ? ArrayUtils.indexOf(column, column[i]) : -1;
            if (index > -1 && index <= chineses.length - 1) {
                keys.add(chineses[index]);
            } else {
                keys.add(column[i]);
            }
        }
        if (data != null && data.size() > 0) {
            list.add(StringUtils.join(keys, ","));
            for (Object obj : data) {
                Map row = JsonUtils.convert(obj, Map.class);
                List<String> rowData = new ArrayList<String>();
                for (String key : column) {
                    String val = !row.containsKey(key) ? "" : row.get(key).toString().replaceAll("\",", "").replaceAll(",", "，").replaceAll("^\\d+$", "'$0");
                    rowData.add("\"" + val + "\"");
                }
                list.add(StringUtils.join(rowData, ","));
            }
        }
        return StringUtils.join(list, "\r\n");
    }
    
    public static String formatToCsvWithRelation(List data, String[] column, String[] chineses) {
        if (column == null && data.size() > 0) {
            Map<String, Object> map;
            if(data.get(0) instanceof Map)
                map=(Map<String, Object>)data.get(0);
            else
                map= BeanMap.create(data.get(0));
            column=new String[map.keySet().size()];
            int i=0;
            for(String c:map.keySet()){
                column[i]=c;
                i++;
            }
        }
        if (chineses == null) chineses = column;
        List<String> list = new ArrayList<String>();
        List<String> keys = new ArrayList<String>();
        for (int i = 0; i < column.length; i++) {
            int index = null != chineses && chineses.length > 0 ? ArrayUtils.indexOf(column, column[i]) : -1;
            if (index > -1 && index <= chineses.length - 1) {
                keys.add(chineses[index]);
            } else {
                keys.add(column[i]);
            }
        }
        list.add(StringUtils.join(keys, ","));
        if (data != null && data.size() > 0) {
            for (Object obj : data) {
                Map row = JsonUtils.convert(obj, Map.class);
                List<String> rowData = new ArrayList<String>();
                for (String key : column) {
                	String [] childKeys=key.split("\\.");
                	if(childKeys.length<2){
                        String val = !row.containsKey(key) ? "" : row.get(key).toString().replaceAll("\",", "").replaceAll(",", "，").replaceAll("^\\d{5,}$", "'$0");
                        rowData.add("\"" + val + "\"");
//                		rowData.add(!row.containsKey(key) ? "" : row.get(key).toString().replaceAll("\\r\\n,", " ").replaceAll(",", "，").replaceAll("^\\d{5,}$", "'$0"));
                	}else{
                		boolean rowValue=false;
                		Map newRow=row;
                		for (int i = 0; i < childKeys.length-1; i++) {
                			if(newRow.get(childKeys[i])==null){
                				rowValue=false;

//                				rowData.add(!row.containsKey(key) ? "" : row.get(key).toString().replaceAll("\\r\\n,", " ").replaceAll(",", "，").replaceAll("^\\d{5,}$", "'$0"));
                                String val = !newRow.containsKey(key) ? "" : newRow.get(key).toString().replaceAll("\",", "").replaceAll(",", "，").replaceAll("^\\d{5,}$", "'$0");
                                rowData.add("\"" + val + "\"");
                				break;
                			}
                			rowValue=true;
                			newRow=JsonUtils.convert(newRow.get(childKeys[i]), Map.class);
						}
                		if(rowValue){
//                			rowData.add(!newRow.containsKey(childKeys[childKeys.length-1]) ? "" : newRow.get(childKeys[childKeys.length-1]).toString().replaceAll("\\r\\n,", " ").replaceAll(",", "，").replaceAll("^\\d{5,}$", "'$0"));
                            String val = !newRow.containsKey(childKeys[childKeys.length-1]) ? "" : newRow.get(childKeys[childKeys.length-1]).toString().replaceAll("\",", "").replaceAll(",", "，").replaceAll("^\\d{5,}$", "'$0");
                            rowData.add("\"" + val + "\"");
                		}
                	}
                }
                list.add(StringUtils.join(rowData, ","));
            }
        }
        return StringUtils.join(list, "\r\n");
    }
}
