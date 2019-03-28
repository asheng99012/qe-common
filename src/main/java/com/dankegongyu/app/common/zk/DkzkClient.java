package com.dankegongyu.app.common.zk;

import com.dankegongyu.app.common.JsonUtils;
import com.dankegongyu.app.common.exception.NeedEmailException;
import com.google.common.base.Charsets;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DkzkClient {
    Logger logger = LoggerFactory.getLogger(DkzkClient.class);
    @Autowired
    public CuratorFramework framework;
    static Map<String, List<Consumer<String>>> watcherMap = new ConcurrentHashMap<>();
    Set<String> paths = new HashSet<>();

    @PostConstruct
    public void init() {
        framework.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                logger.info("zk 状态表更：" + JsonUtils.toJson(newState));
                if (newState == ConnectionState.LOST) {
                    paths.stream().forEach(val -> {
                        logger.info("开始重新绑定监控事件：{}", val);
                        proxyChangeEvent(val);
                    });
                }
            }
        });
    }

    public <T> void createOrUpdateNode(String path, String val) {
        try {
            if (framework.checkExists().forPath(path) == null) {
                if (val != null)
                    framework.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, val.getBytes(Charsets.UTF_8));
                else
                    framework.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            } else {
                if (val != null)
                    framework.setData().forPath(path, val.getBytes(Charsets.UTF_8));
            }
        } catch (Exception e) {
            throw new NeedEmailException(e.getMessage(), e.getCause());
        }
    }

    public void deleteNode(String path) {
        try {
            if (framework.checkExists().forPath(path) != null) {
                framework.delete().deletingChildrenIfNeeded().forPath(path);
            }
        } catch (Exception e) {
            throw new NeedEmailException(e.getMessage(), e.getCause());
        }
    }

    public String readNode(String path) {
        try {
            if (framework.checkExists().forPath(path) != null) {
                return new String(framework.getData().forPath(path));
            }
            return null;
        } catch (Exception e) {
            throw new NeedEmailException(e.getMessage(), e.getCause());
        }
    }

    public void watchChange(String path, Consumer<String> function) {
        logger.info("添加变更监控|{}", path);
        if (!watcherMap.containsKey(path)) {
            watcherMap.put(path, new ArrayList<>());
        }
        watcherMap.get(path).add(function);
    }

    public void proxyChangeEvent(String path) {
        if (paths.contains(path)) return;
        paths.add(path);
        logger.info("开始监路径:{}", path);
        createOrUpdateNode(path, null);
        TreeCache cache = new TreeCache(framework, path);
        cache.getListenable().addListener((client1, event) -> {
            logger.info(JsonUtils.toJson(event));
            if (event.getData() != null) {
                String _path = event.getData().getPath();
                TreeCacheEvent.Type type = event.getType();
                logger.info("路径:{}|事件类型:{},事件:{}", _path, type, watcherMap.containsKey(_path));
                if (type.equals(TreeCacheEvent.Type.NODE_ADDED) || type.equals(TreeCacheEvent.Type.NODE_UPDATED)) {
                    if (watcherMap.containsKey(_path)) {
                        logger.info("data:" + new String(event.getData().getData(), Charsets.UTF_8));
                        watcherMap.get(_path).forEach(action -> action.accept(new String(event.getData().getData(), Charsets.UTF_8)));
                    }
                }
            }
        });
        try {
            cache.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new NeedEmailException(e.getMessage(), e.getCause());
        }
    }

}
