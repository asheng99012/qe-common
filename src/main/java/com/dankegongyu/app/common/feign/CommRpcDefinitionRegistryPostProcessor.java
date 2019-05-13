package com.dankegongyu.app.common.feign;

import com.dankegongyu.app.common.AppUtils;
import com.dankegongyu.app.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

public class CommRpcDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    Logger logger = LoggerFactory.getLogger(CommRpcDefinitionRegistryPostProcessor.class);

    Environment env;

    public CommRpcDefinitionRegistryPostProcessor(Environment env) {
        this.env = env;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        try {
            String locations = env.getProperty("commRpc.locations");
            String[] ls = StringUtils.tokenizeToStringArray(locations, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (String location : ls) {
                logger.info("开始解析：" + location);
                Resource[] resources = new PathMatchingResourcePatternResolver().getResources(location);
                if (resources == null || resources.length == 0) {
                    logger.error("未找到：" + location);
                    continue;
                }
                for (Resource resource : resources) {
                    Map config = new Yaml().loadAs(resource.getInputStream(), Map.class);
                    Map commRpc = JsonUtils.convert(config.get("commRpc"), Map.class);
                    String proxyName = commRpc.get("proxy").toString();
                    Class<ICommRpc> proxyClass = (Class<ICommRpc>) Class.forName(proxyName);
                    List list = JsonUtils.convert(commRpc.get("list"), List.class);
                    for (Object item : list) {
                        String serviceName = item.toString();
                        Class serviceClass = Class.forName(serviceName);
                        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(serviceClass);
                        GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
                        definition.getPropertyValues().add("service", serviceClass);
                        definition.getPropertyValues().add("commRpc", proxyClass);
                        definition.setBeanClass(CommRpcProxy.class);
                        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                        registry.registerBeanDefinition(serviceName, definition);
                    }
                }
            }
        } catch (Exception e) {
            throw new BeanCreationException(e.getMessage(), e.getCause());
        }

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
