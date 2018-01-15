package com.dankegongyu.app.common.exception;

/**
 * Created by ashen on 2017-2-12.
 */
public class NeedLoginException extends BaseException {
    public NeedLoginException() {
        super();
    }

    public NeedLoginException(String message) {
        super(message);
    }

    public NeedLoginException(String message, Throwable cause) {
        super(message, cause);
    }


    public NeedLoginException(Throwable cause) {
        super(cause);
    }


    protected NeedLoginException(String message, Throwable cause,
                                    boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    @Override
    public int getCode() {
        return 2;
    }
}
