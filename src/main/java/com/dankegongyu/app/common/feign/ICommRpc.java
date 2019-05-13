package com.dankegongyu.app.common.feign;

import org.springframework.web.bind.annotation.*;

@RequestMapping("/")
public interface ICommRpc {

    @ResponseBody
    @PostMapping("/commrpc/{service}/{action}")
    public Object exec(@PathVariable("service") String service, @PathVariable("action") String action, @RequestBody Object[] args);
}
