package com.dankegongyu.app.common;

import com.dankegongyu.app.common.exception.BaseException;
import com.dankegongyu.app.common.exception.NeedLoginException;
import com.dankegongyu.app.common.exception.ParameterException;
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

    public static String getErrorMsg() {
        String error = Current.getSession(GlobalExceptionHandler.currentSessionError);
        Current.removeSession(GlobalExceptionHandler.currentSessionError);
        return error;
    }

    public static ApiResult getErrorResult(Exception e) {
        ApiResult result = new ApiResult("出错了，请稍后再试", 1);
        if(e instanceof BaseException){
            result.setMsg(e.getMessage());
            result.setStatus(((BaseException)e).getCode());
        }
        return result;
    }


    @ExceptionHandler
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if(ex instanceof ParameterException || ex instanceof NeedLoginException){
            logger.warn(ex.getMessage());
        }
        else{
            logger.error(ex.getMessage(), ex);
        }
        ex.printStackTrace();
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
