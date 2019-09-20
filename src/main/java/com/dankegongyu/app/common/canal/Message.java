package com.dankegongyu.app.common.canal;

import com.dankegongyu.app.common.JsonUtils;
import com.google.common.base.Strings;

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
        if (message.getData() != null && message.getData().size() > 0) {
            int len = message.getData().size();
            for (int i = 0; i < len; i++) {
                Message msg = new Message();
                msg.setDatabase(message.getDatabase());
                msg.setTable(message.getTable());
                msg.setType(message.getType());
                msg.setExecuteTime(message.getEs());
                msg.setMysqlType(message.getMysqlType());
                msg.setData(message.getData().get(i));
                if (message.getOld() != null && message.getOld().size() > i)
                    msg.setOld(message.getOld().get(i));
                list.add(msg);
            }
        } else {
            Message msg = new Message();
            msg.setDatabase(message.getDatabase());
            msg.setTable(message.getTable());
            msg.setType(message.getType());
            msg.setExecuteTime(message.getEs());
            msg.setMysqlType(message.getMysqlType());
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

    public boolean isUpdate(String column) {
        return isUpdate() && old != null && old.containsKey(column);
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

}
