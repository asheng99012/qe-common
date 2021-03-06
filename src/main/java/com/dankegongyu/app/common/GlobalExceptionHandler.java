package com.dankegongyu.app.common;

import com.dankegongyu.app.common.exception.BaseException;
import com.dankegongyu.app.common.exception.NeedEmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by ashen on 2017-2-9.
 */
public class GlobalExceptionHandler implements HandlerExceptionResolver {
    public static String currentSessionError = "currentSessionError";
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static boolean warpError = true;

    public static void setCurrentThreadError(String msg) {
        Current.set(GlobalExceptionHandler.class.getName() + ".CurrentThreadError", msg);
    }

    public static String getCurrentThreadError() {
        return Current.get(GlobalExceptionHandler.class.getName() + ".CurrentThreadError");
    }

    public void setWarpError(boolean warpError) {
        GlobalExceptionHandler.warpError = warpError;
    }

    public static String getErrorMsg() {
        String error = Current.getSession(GlobalExceptionHandler.currentSessionError);
        Current.removeSession(GlobalExceptionHandler.currentSessionError);
        return error;
    }

    public static Throwable getRealThrowable(Throwable e) {
        Throwable t = e.getCause();
        if (t == null) {
            return e;
        } else {
            return getRealThrowable(t);
        }

    }

    public static ApiResult getErrorResult(Exception e) {
        ApiResult result = new ApiResult("出错了，请稍后再试", 1);
        logger.error(e.getMessage(), e);
        Throwable t = getRealThrowable(e);
        if (!warpError)
            result.setMsg(t.getMessage() == null ? "空指针" : t.getMessage());
        if (t instanceof BaseException) {
            result.setMsg(t.getMessage());
            result.setStatus(((BaseException) t).getCode());
        } else {
            String msg = t.getMessage();
            if (msg != null && msg.startsWith("com.dankegongyu.app.common.exception")) {
                String line = java.security.AccessController.doPrivileged(
                        new sun.security.action.GetPropertyAction("line.separator"));
                msg = msg.substring(0, msg.indexOf(line));
                msg = msg.substring(msg.indexOf(": ") + 1);
                result.setMsg(msg);
            }
        }
        Current.sendErrorMsg(t);
//        logger.error(t.getMessage(), t);
        return result;
    }


    @ExceptionHandler
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ApiResult result = getErrorResult(ex);
        if (Current.isAjax()) {
            BaseController.writeJsonToClient(result);
            return new ModelAndView("page/json");
        } else {
            Current.getRequest().setAttribute(BaseController.MODEL, result);
            return new ModelAndView("page/error");
        }
    }

}
