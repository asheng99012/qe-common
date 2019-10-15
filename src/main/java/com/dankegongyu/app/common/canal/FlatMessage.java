package com.dankegongyu.app.common.canal;

import com.dankegongyu.app.common.JsonUtils;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlatMessage {
    private String database;
    private String table;
    private String type;
    private Date es;
    private Map<String, String> mysqlType;
    private List<Map<String, Object>> data;
    private List<Map<String, Object>> old;

    public FlatMessage fromJson(String json) {
        FlatMessage message = JsonUtils.convert(json, FlatMessage.class);
        convert(message.data, message.mysqlType);
        convert(message.old, message.mysqlType);
        return message;
    }

    private void convert(List<Map<String, Object>> dataList, Map<String, String> type) {
        if (dataList != null && dataList.size() > 0) {
            for (Map<String, Object> aDataList : dataList) {
                for (Map.Entry<String, Object> entry : aDataList.entrySet()) {
                    if (entry.getValue() != null && !Strings.isNullOrEmpty(entry.getValue().toString()))
                        entry.setValue(Mapping.converter(type.get(entry.getKey()), entry.getValue().toString()));
                }
            }
        }
    }
}
