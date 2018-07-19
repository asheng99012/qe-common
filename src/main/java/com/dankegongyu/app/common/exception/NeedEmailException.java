package com.dankegongyu.app.common.exception;

/**
 * Created by ashen on 2017-2-12.
 */
public class NeedEmailException extends BaseException {
    public NeedEmailException() {
        super();
    }

    public NeedEmailException(String message) {
        super(message);
    }

    public NeedEmailException(String message, Throwable cause) {
        super(message, cause);
    }


    public NeedEmailException(Throwable cause) {
        super(cause);
    }


    protected NeedEmailException(String message, Throwable cause,
                                 boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    @Override
    public int getCode() {
        return 1;
    }
}
