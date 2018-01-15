package com.dankegongyu.app.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by ashen on 2017-3-10.
 */
@Component
public class SystemConfig {
    public static final String ENV_DEV = "dev";
    public static final String ENV_TEST = "test";
    public static final String ENV_PRE = "pre";
    public static final String ENV_PROD = "prod";
    public static final String ENV_UNIT = "unit";
    @Value("${sysConfig.env}")
    private String env;
    @Value("${sysConfig.pcHost}")
    private String pcHost;
    @Value("${sysConfig.h5Host}")
    private String h5Host;
    @Value("${sysConfig.apiHost}")
    private String apiHost;
    @Value("${sysConfig.adminHost}")
    private String adminHost;
    @Value("${sysConfig.corpUserId}")
    private String corpUserId;

    public String getCorpUserId() {
        return corpUserId;
    }

    public void setCorpUserId(String corpUserId) {
        this.corpUserId = corpUserId;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getPcHost() {
        return pcHost;
    }

    public void setPcHost(String pcHost) {
        this.pcHost = pcHost;
    }

    public String getH5Host() {
        return h5Host;
    }

    public void setH5Host(String h5Host) {
        this.h5Host = h5Host;
    }

    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getAdminHost() {
        return adminHost;
    }

    public void setAdminHost(String adminHost) {
        this.adminHost = adminHost;
    }

    public static SystemConfig getInstance() {
        return Current.getBean(SystemConfig.class);
    }
}
