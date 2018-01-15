package com.dankegongyu.app.common;

import java.util.List;

/**
 * Created by asheng on 2015/5/29 0029.
 */
public class ApiResultListData<T> {
    List<T> list;
    Integer count;

    public ApiResultListData() {
    }

    public ApiResultListData(List<T> list, Integer count) {
        this.setList(list);
        this.setCount(count);
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}

