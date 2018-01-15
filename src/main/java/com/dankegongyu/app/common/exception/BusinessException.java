package com.dankegongyu.app.common.exception;

/**
 * Created by ashen on 2017-2-12.
 */
public class BusinessException extends BaseException {
    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }


    public BusinessException(Throwable cause) {
        super(cause);
    }


    protected BusinessException(String message, Throwable cause,
                                 boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    @Override
    public int getCode() {
        return 1;
    }
}
