package com.dankegongyu.app.common;

/**
 * Created by asheng on 2015/5/29 0029.
 */
public class ApiResult<T> {
    private Integer status;
    private String msg;
    private T data;


    public ApiResult() {
        this.status = 0;
    }

    public ApiResult(T data) {
        this.status = 0;
        this.data = data;
    }

    public ApiResult(String msg) {
        this.status = 1;
        this.msg = msg;
    }

    public ApiResult(String msg, Integer code) {
        this.status = code;
        this.msg = msg;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
