package com.dankegongyu.app.common;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class JsonRequestBodyAdviceAdapter extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    @Nullable
    public Object handleEmptyBody(@Nullable Object body, HttpInputMessage inputMessage,
                                  MethodParameter parameter, Type targetType,
                                  Class<? extends HttpMessageConverter<?>> converterType) {
        Map data = FormFilter.getParameters();
        if (isArray(targetType) && data.containsKey("data"))
            return JsonUtils.convert(data.get("data"), targetType);
        else
            return JsonUtils.convert(data, targetType);
    }

    public boolean isArray(Type targetType) {
        if (targetType.getTypeName().equals("java.lang.Object[]"))
            return true;
        if (targetType instanceof ParameterizedType && ((ParameterizedType) targetType).getRawType() == List.class)
            return true;
        return false;
    }
}
