package com.dankegongyu.app.common.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;

public class DkClientFactory {
    public static CuratorFramework build(String connectString, int sessionTimeoutMs, int connectionTimeoutMs, RetryPolicy retryPolicy) {
        return build(connectString, sessionTimeoutMs, connectionTimeoutMs, retryPolicy, null);
    }

    public static CuratorFramework build(String connectString, int sessionTimeoutMs, int connectionTimeoutMs, RetryPolicy retryPolicy, String namespace) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .sessionTimeoutMs(sessionTimeoutMs)
                .connectionTimeoutMs(connectionTimeoutMs)
                .retryPolicy(retryPolicy);
        if (namespace != null)
            builder.namespace(namespace);
        CuratorFramework client = builder.build();
        return client;
    }
}
