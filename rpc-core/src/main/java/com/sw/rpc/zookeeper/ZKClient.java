package com.sw.rpc.zookeeper;

import com.sw.rpc.utils.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZKClient {
    private ZooKeeper zooKeeper;
    private Map<String, List<String>> pathChildrenMap = new ConcurrentHashMap<>();


    public void createPersistentPathIfNotExist(String path) throws InterruptedException, KeeperException, IOException {
        String[] pathElems = path.split("/");
        String tmpPath = "";
        for (String pathElem : pathElems) {
            if ("".equals(pathElem)) {
                continue;
            }
            tmpPath += "/" + pathElem;
            // 不存在则创建
            if (zooKeeper.exists(tmpPath, null) == null) {
                zooKeeper.create(tmpPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }
    }

    public void checkAndCreateTmpPath(String path) throws InterruptedException, KeeperException {
        if (zooKeeper.exists(path, null) == null) {
            zooKeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } else {
            throw new RuntimeException("fail to register service because address has already exist in zookeeper,path:" + path);
        }
    }

    public List<String> getChildrenOfPath(String path) throws InterruptedException, KeeperException {
        if (pathChildrenMap.containsKey(path)) {
            return pathChildrenMap.get(path);
        } else {
            synchronized (this) {
                if (pathChildrenMap.containsKey(path)) {
                    return pathChildrenMap.getOrDefault(path, null);
                } else {
                    // 如果 path 不存在会抛出异常
                    List<String> children = zooKeeper.getChildren(path, new ChildrenWatcher(path));
                    if (children == null) {
                        // 空列表, zookeeper 中 path 将会被删除，因此不能放到 map 中，否则一直不会更新
                        return null;
                    }
                    pathChildrenMap.put(path, children);
                    return children;
                }
            }
        }
    }


    public ZKClient(String connectionString, int sessionTimeOut) throws IOException {
        try {
            zooKeeper = new ZooKeeper(connectionString, sessionTimeOut, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    log.info("connect to zookeeper successfully!");
                }
            });
        } catch (Exception e) {
            log.error("create zookeeper client fail");
            throw e;
        }
    }


    class ChildrenWatcher implements Watcher {
        String watchPath;

        @Override
        public void process(WatchedEvent watchedEvent) {
            switch (watchedEvent.getType()) {
                case NodeDeleted:
                    log.info("path has been removed,path:{}", watchPath);
                    pathChildrenMap.remove(watchPath);
                    break;
                case NodeChildrenChanged:
                    try {
                        log.info("path children has been changed,path:{}", watchPath);
                        // 获取子节点,自举添加监听
                        List<String> children = zooKeeper.getChildren(watchPath, this);
                        // 更新缓存
                        if (CollectionUtil.isEmpty(children)) {
                            // 没有子节点了，删除
                            zooKeeper.delete(watchPath, Version.REVISION);
                            pathChildrenMap.remove(watchPath);
                        } else {
                            // 还有子节点，更新
                            pathChildrenMap.put(watchPath, children);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    break;
                default:
            }
        }

        public ChildrenWatcher(String path) {
            this.watchPath = path;
        }
    }
}
