package com.dankegongyu.app.common.feign;

import com.alibaba.fastjson.JSONArray;
import com.dankegongyu.app.common.BaseController;
import com.dankegongyu.app.common.RpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.InvocationTargetException;

//@Controller
//@RequestMapping("/")
public class CommRpc extends BaseController {
    @Autowired
    RpcService rpcService;

    @ResponseBody
    @RequestMapping("/rpc/{service}/{action}")
    public Object callback(@PathVariable("service") String service, @PathVariable("action") String action) throws ClassNotFoundException, InvocationTargetException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        JSONArray data = getParameter("data");
        if (data == null) data = new JSONArray();
        Object ret = rpcService.run(new Object[]{service, action}, data.toArray());
        return ret;
    }

}
