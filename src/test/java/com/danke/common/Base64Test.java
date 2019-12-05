package com.danke.common;

import com.dankegongyu.app.common.JsonUtils;
import lombok.extern.java.Log;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.springframework.util.Base64Utils;

import java.util.HashMap;
import java.util.Map;

@Log
public class Base64Test {

    @Test
    public void run(){
        Map map=new HashMap(){{
            put("a","aa");
            put("b","bb");
        }};
        String json= JsonUtils.toJson(map);
        String ret= Base64Utils.encodeToString(json.getBytes());
        String ret2=new String(Base64Utils.decodeFromString(ret));
        log.info("ok");
    }


    @Test
    public void testMd5(){
        String ret= DigestUtils.md5Hex("zjsis===============================================================================iss");
        System.out.println(ret);
    }
}
