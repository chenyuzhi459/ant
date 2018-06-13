package io.sugo.zookeeper;

import io.sugo.zookeeper.client.CuratorZookeeperClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.ReentrantLock;

public class ZKClientHandler {
//    private static final Logger LOG = Logger.getLogger(ZKClientHandler.class);
    private static final Logger LOG = LogManager.getLogger(ZKClientHandler.class);
    private final ReentrantLock lock = new ReentrantLock();
    private String clientId;
    private CuratorZookeeperClient client;

    public ZKClientHandler(String clientId, CuratorZookeeperClient client) {
        this.clientId = clientId;
        this.client = client;
    }

    public CuratorZookeeperClient getClient() {
//        lock.lock();
        LOG.info("clientId:"+clientId);
//        LOG.info("Hash:"+this);
        return client;
    }

    public void close() {
        client.close();
    }
}
