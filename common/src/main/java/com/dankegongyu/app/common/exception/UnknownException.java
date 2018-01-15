package com.dankegongyu.app.common.exception;

/**
 * Created by ashen on 2017-2-12.
 */
public class UnknownException extends BaseException {

    public UnknownException() {
        super();
    }

    public UnknownException(String message) {
        super(message);
    }

    public UnknownException(String message, Throwable cause) {
        super(message, cause);
    }


    public UnknownException(Throwable cause) {
        super(cause);
    }


    protected UnknownException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public int getCode() {
        return 1;
    }
}
