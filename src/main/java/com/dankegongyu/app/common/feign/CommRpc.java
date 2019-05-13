package com.dankegongyu.app.common.feign;

import com.alibaba.fastjson.JSONArray;
import com.dankegongyu.app.common.BaseController;
import com.dankegongyu.app.common.RpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;

import static com.dankegongyu.app.common.BaseController.getParameter;

@RestController
public class CommRpc implements ICommRpc {
    @Autowired
    RpcService rpcService;

    @Override
    public Object exec(String service, String action, Object[] args) {
        try {
            return rpcService.run(new Object[]{service, action}, args);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
