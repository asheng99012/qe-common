package com.dankegongyu.app.common.exception;

/**
 * Created by ashen on 2017-2-12.
 */
public abstract class BaseException extends RuntimeException {

    public BaseException() {
        super();
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }


    public BaseException(Throwable cause) {
        super(cause);
    }


    protected BaseException(String message, Throwable cause,
                            boolean enableSuppression,
                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    public abstract int getCode();
}
