package com.dankegongyu.app.common.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dankegongyu.app.common.RpcService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 配合 proxy 使用的异步任务处理
 */
public class CommonListener extends BaseListener {
    Logger logger = LoggerFactory.getLogger(CommonListener.class);
    @Autowired
    RpcService rpcService;

    @Override
    public void exec(Message message, Channel channel) {
        JSONObject data = JSON.parseObject(getBody(message));
        try {
            rpcService.run(new Object[]{data.get("class"), data.get("method")}, JSON.parseArray(data.get("data").toString()).toArray());
        } catch (Exception e) {
            logger.error(getBody(message) + "|" + e.getMessage(), e.getCause());
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
