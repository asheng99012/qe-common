package com.dankegongyu.app.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于模拟HTTP请求中GET/POST方式 
 * @author landa
 *
 */
public class HttpUtils {  
    /** 
     * 发送GET请求 
     *  
     * @param url 
     *            目的地址 
     * @param parameters 
     *            请求参数，Map类型。 
     * @return 远程响应结果 
     */  
	private static int state = -1;
	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	
    public static String sendGet(String url, Map<String, String> parameters) { 
        String result="";
        int counts = 0;
        BufferedReader in = null;// 读取响应输入流  
        StringBuffer sb = new StringBuffer();// 存储参数  
        String params = "";// 编码之后的参数
        java.net.HttpURLConnection httpConn = null;
        try {
        	// 编码请求参数  
        	if(parameters.size()==1){
        		for(String name:parameters.keySet()){
        			sb.append(name).append("=").append(
                           java.net.URLEncoder.encode(parameters.get(name),  
                            "UTF-8"));
        		}
        			params=sb.toString();
        	}else{
        		for (String name : parameters.keySet()) {  
        			sb.append(name).append("=").append(  
                           java.net.URLEncoder.encode(parameters.get(name),  
                                    "UTF-8")).append("&");  
        		}  
        		String temp_params = sb.toString();  
        		params = temp_params.substring(0, temp_params.length() - 1);  
        	}
        	String full_url = url + "?" + params; 
        	logger.info("url为="+full_url);
        	// 创建URL对象  
            java.net.URL connURL = new java.net.URL(full_url);
        		
            if (httpConn==null) {
                httpConn = (java.net.HttpURLConnection) connURL  
                            .openConnection();
                // 设置通用属性  
            	httpConn.setRequestProperty("Accept", "*/*");  
            	httpConn.setRequestProperty("Connection", "Keep-Alive");  
            	httpConn.setRequestProperty("User-Agent",  
                        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
    		}  
        	state = httpConn.getResponseCode();
        	logger.info("响应的state为"+state);
        	if (state == 200) {
        			  
            	// 建立实际的连接  
            	//httpConn.connect();  
            	// 响应头部获取  
            	Map<String, List<String>> headers = httpConn.getHeaderFields();  
            	// 遍历所有的响应头字段  
            		
            	// 定义BufferedReader输入流来读取URL的响应,并设置编码方式  
            	in = new BufferedReader(new InputStreamReader(httpConn  
                        .getInputStream(), "UTF-8"));  
            	String line;  
            	// 读取返回的内容  
            	while ((line = in.readLine()) != null) {  
            		result += line;  
            	}
			}else {
				logger.error("连接失败");
				    
			}
        		  
        } catch (Exception e) {
        }finally{
        	try {  
        		if (in != null) {  
        			in.close();  
        		}
        		if (httpConn != null) {
					httpConn.disconnect();
				}
        	} catch (IOException ex) {  
        	}  
        }
        
        return result ;
    }  
  
    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.error("发送 POST 请求出现异常！"+e);
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
            }
        }
        return result;
    } 
  
    /** 
     * 主函数，测试请求 
     *  
     * @param args 
     */  
    public static void main(String[] args) {  
    	final String s = "INTERNAL SERV1CE sa1t";
    	Long timestamp = System.currentTimeMillis()/1000;
    	String token = s + timestamp;
    	
        Map<String, String> parameters = new HashMap<String, String>(); 
        
        parameters.put("roomId", "506298033");  
        parameters.put("token", "6cce3fd564029dee887276b0ca65d9b91423c796"); 
        parameters.put("timestamp", timestamp+""); 
        String result =sendGet("http://www.dankegongyu.com/internal-service/room/public-room-id", parameters);
        
//      String result2 =sendPost("http://192.168.193.7:8081/ding/community/search", parameters2);
        
        System.out.println(result); 
        //System.out.println(result2);
    }  
}