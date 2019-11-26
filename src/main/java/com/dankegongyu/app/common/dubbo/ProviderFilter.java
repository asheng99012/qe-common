package com.dankegongyu.app.common.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSON;
import com.dankegongyu.app.common.CurrentContext;
import com.dankegongyu.app.common.TraceIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ProviderFilter implements Filter {
    Logger logger = LoggerFactory.getLogger(ProviderFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Map<String, String> attachments = invocation.getAttachments();
        if (null != attachments) {
            attachments.remove(Constants.ASYNC_KEY);
        }

        if (attachments.containsKey(CurrentContext.class.getName())) {
            CurrentContext.resetFromJson(attachments.get(CurrentContext.class.getName()));
        }
        TraceIdUtils.setTraceId();
        try {
            Result result = invoker.invoke(invocation);
            if (result.hasException()) {
                logger.error(result.getException().getMessage(), result.getException().getCause());
            }
            return result;
        } finally {
            CurrentContext.clear();
            //todo 不能清空
        }

    }
}
