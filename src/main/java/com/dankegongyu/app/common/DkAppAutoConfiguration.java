package com.dankegongyu.app.common;

import com.dankegongyu.app.common.feign.DkFeignContext;
import com.dankegongyu.app.common.feign.FeignFilter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.sleuth.instrument.web.client.feign.TraceFeignClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@AutoConfigureAfter({TraceFeignClientAutoConfiguration.class})
public class DkAppAutoConfiguration {
    @Bean
    public DkFeignContext.DKFeignContextBeanPostProcessor dkFeignContextBeanPostProcessor(BeanFactory beanFactory) {
        return new DkFeignContext.DKFeignContextBeanPostProcessor(beanFactory);
    }
}
