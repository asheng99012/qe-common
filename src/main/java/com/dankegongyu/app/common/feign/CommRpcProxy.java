package com.dankegongyu.app.common.feign;


import com.dankegongyu.app.common.AppUtils;
import com.dankegongyu.app.common.JsonUtils;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CommRpcProxy<T> implements MethodInterceptor, FactoryBean<T> {
    private Class<T> service;
    private Class<ICommRpc> commRpc;

    public CommRpcProxy() {
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        String interfaceName = service.getCanonicalName();
        String methodName = method.getName();
        ICommRpc proxy = AppUtils.getBean(commRpc);
        Object ret = proxy.exec(interfaceName, methodName, objects);
        return JsonUtils.convert(ret, method.getGenericReturnType());
    }

    @Override
    public T getObject() throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(new Class[]{service});
        enhancer.setCallback(this);
        return (T) enhancer.create();
    }

    public void setService(Class<T> service) {
        this.service = service;
    }

    public Class<T> getService() {
        return service;
    }

    public Class<ICommRpc> getCommRpc() {
        return commRpc;
    }

    public void setCommRpc(Class<ICommRpc> commRpc) {
        this.commRpc = commRpc;
    }

    @Override
    public Class<T> getObjectType() {
        return this.service;
    }

}
