package com.dankegongyu.app.common;

/**
 * Created by asheng on 2015/5/29 0029.
 */
public class ApiResult<T> {
    private Integer status;
    private String message;
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
        this.message = msg;
    }

    public ApiResult(String msg, Integer code) {
        this.status = code;
        this.message = msg;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public void setData(T data) {
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
    public String getMsg() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public void setMsg(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }
}
