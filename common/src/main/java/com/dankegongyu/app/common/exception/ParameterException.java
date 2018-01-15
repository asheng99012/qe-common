package com.dankegongyu.app.common.exception;

/**
 * Created by ashen on 2017-2-12.
 */
public class ParameterException extends BaseException {
    public ParameterException() {
        super();
    }

    public ParameterException(String message) {
        super(message);
    }

    public ParameterException(String message, Throwable cause) {
        super(message, cause);
    }


    public ParameterException(Throwable cause) {
        super(cause);
    }


    protected ParameterException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public int getCode() {
        return 1;
    }
}
