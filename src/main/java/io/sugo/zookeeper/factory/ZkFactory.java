package io.sugo.zookeeper.factory;

import io.sugo.cache.Cache;
import io.sugo.http.Configure;
import io.sugo.zookeeper.ZKClientHandler;
import io.sugo.zookeeper.client.CuratorZookeeperClient;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class ZkFactory {
    private static ZkFactory zkFactory = new ZkFactory();

    private ZkFactory() {}

    public static ZkFactory getFactory() {
//        configure = conf;
        return zkFactory;
    }

    public CuratorZookeeperClient newClient(String servers) {
        return new CuratorZookeeperClient(servers);
    }


    public ZKClientHandler getClientHandler() throws ExecutionException {
        String[] zkServers = Configure.getConfigure().getProperty("zk.properties","zk.servers").split(",");
        Arrays.sort(zkServers);
        return Cache.getZkClientCache().get(Arrays.toString(zkServers));
    }
}
