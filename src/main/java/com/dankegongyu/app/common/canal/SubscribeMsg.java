package com.dankegongyu.app.common.canal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeMsg {
    /**
     * 过滤规则
     */
    private Map<String, Object> filterConfig;
    /**
     * 数据格式
     */
    private List<FlatMessage> flatMessage;
}
