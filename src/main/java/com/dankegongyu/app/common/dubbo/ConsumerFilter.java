package com.dankegongyu.app.common.dubbo;

import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dankegongyu.app.common.CurrentContext;

import java.util.Map;

public class ConsumerFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Map<String, String> _map = RpcContext.getContext().getAttachments();
        _map.remove(CurrentContext.class.getName());
        Map<String, String> map = invocation.getAttachments();
        map.put(CurrentContext.class.getName(), CurrentContext.toJson());
        map.putAll(_map);
        return invoker.invoke(invocation);
    }
}
