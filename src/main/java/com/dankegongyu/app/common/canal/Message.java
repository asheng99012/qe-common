package com.dankegongyu.app.common.canal;

import com.dankegongyu.app.common.JsonUtils;

import javax.swing.plaf.PanelUI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Message {
    private String database;
    private String table;
    private String type;
    private Date executeTime;
    private Map<String, String> mysqlType;
    private Map<String, Object> data;
    private Map<String, Object> old;
    public static final String TYPE_INSERT = "INSERT";
    public static final String TYPE_UPDATE = "UPDATE";
    public static final String TYPE_DELETE = "DELETE";

    public static List<Message> fromJson(String json) {
        List<Message> list = new ArrayList<>();
        FlatMessage message = (new FlatMessage()).fromJson(json);
        if (message.data != null && message.data.size() > 0) {
            int len = message.data.size();
            for (int i = 0; i < len; i++) {
                Message msg = new Message();
                msg.setDatabase(message.database);
                msg.setTable(message.table);
                msg.setType(message.type);
                msg.setExecuteTime(message.es);
                msg.setMysqlType(message.mysqlType);
                msg.setData(message.data.get(i));
                if (message.old != null && message.old.size() > i)
                    msg.setOld(message.old.get(i));
                list.add(msg);
            }
        } else {
            Message msg = new Message();
            msg.setDatabase(message.database);
            msg.setTable(message.table);
            msg.setType(message.type);
            msg.setExecuteTime(message.es);
            msg.setMysqlType(message.mysqlType);
            list.add(msg);
        }
        return list;
    }

    public boolean isInsert() {
        return type.equalsIgnoreCase(TYPE_INSERT);
    }

    public boolean isUpdate() {
        return type.equalsIgnoreCase(TYPE_UPDATE);
    }

    public boolean isDelete() {
        return type.equalsIgnoreCase(TYPE_DELETE);
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }

    public Map<String, String> getMysqlType() {
        return mysqlType;
    }

    public void setMysqlType(Map<String, String> mysqlType) {
        this.mysqlType = mysqlType;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getOld() {
        return old;
    }

    public void setOld(Map<String, Object> old) {
        this.old = old;
    }

    private static class FlatMessage {
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
                        if (entry.getValue() != null)
                            entry.setValue(Mapping.converter(type.get(entry.getKey()), entry.getValue().toString()));
                    }
                }
            }
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setEs(Date es) {
            this.es = es;
        }

        public void setMysqlType(Map<String, String> mysqlType) {
            this.mysqlType = mysqlType;
        }

        public void setData(List<Map<String, Object>> data) {
            this.data = data;
        }

        public void setOld(List<Map<String, Object>> old) {
            this.old = old;
        }
    }

}
