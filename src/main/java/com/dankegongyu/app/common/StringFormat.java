package com.dankegongyu.app.common;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by asheng on 2015/5/16 0016.
 */
public class StringFormat {

    public static String format(String str, Object... args) {
        for (int i = 0; i < args.length; i++) {
            String arg = "";
            if (args[i] != null) arg = String.valueOf(args[i]);
            str = str.replaceAll("\\{" + i + "\\}", arg);
        }
        return str;
    }

    public static String format(String str, Map<String, Object> map) {
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String arg = "";
            if (map.get(key) != null) arg = (String) map.get(key);
            str = str.replaceAll("\\{" + key + "\\}", (String) map.get(key));
        }
        return str;
    }

    public static String format(String str, Object object) {
        try {
            Map<String, Object> map = JsonUtils.convert(object, HashMap.class);
            return format(str, map);
        } catch (Exception e) {
            return format(str, new Object[]{object});
        }

    }

    public static String format(String str, AbstractRegexOperator operator) {
        Pattern p = Pattern.compile(operator.getPattern());
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, operator.exec(m));
        }
        m.appendTail(sb);
        return sb.toString();
    }
    
    public static String parseTime(String time) {  
    	if(StringUtils.isEmpty(time)) return null;
    	int seconds = Integer.parseInt(time);
    	int temp=0;
    	StringBuffer sb=new StringBuffer();
    	temp = seconds/3600;
    	sb.append((temp<10)?"0"+temp+":":""+temp+":");

    	temp=seconds%3600/60;
    	sb.append((temp<10)?"0"+temp+":":""+temp+":");

    	temp=seconds%3600%60;
    	sb.append((temp<10)?"0"+temp:""+temp);
    	
        return sb.toString();  
    }
    
    public static String hideMiddleStr(String oldStr,String hideStr,int begin,int end){
    	return oldStr.substring(0, begin).concat(hideStr).concat(oldStr.substring(end));
    }

}