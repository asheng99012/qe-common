package com.dankegongyu.app.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asheng on 2015/5/29 0029.
 */
public class ApiResultList<T> extends ApiResult {
    ApiResultListData<T> data = new ApiResultListData<T>();

    public ApiResultList() {
    }

    public ApiResultList(ApiResultListData<T> data) {
        this.data = data;
    }

    public ApiResultList(List<T> list, int count) {
        if (list == null)
            list = new ArrayList();
        data.setList(list);
        data.setCount(count);
        this.setStatus(0);
    }

    public void setData(ApiResultListData<T> data) {
        this.data = data;
    }

    public ApiResultListData<T> getData() {
        return data;
    }
}

