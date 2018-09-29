/*
 * Copyright (C) 2012 GZ-ISCAS Inc., All Rights Reserved.
 */
package io.sugo.common.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.eclipse.jetty.util.StringUtil;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * @Description: 全局配置参数  
 * @Author janpychou@qq.com
 * @CreateDate:   [May 26, 2015 8:04:08 PM]   
 *
 */
public class Constants {
    private static final Logger log = LogManager.getLogger(Constants.class);

    public static final String SYSTEM_PROPS = "system.properties";
    public static final String HIVE_PROPS = "hive.properties";

    public static class Sys{

        public static final String PROXY_CONN_READ_TIMEOUT = "proxyConnReadTimeout";

        public static final String SERVER_MAX_CONN = "server.max.conn";
        public static final String HTTP_PORT = "http.port";
        public static final String REDIS_CONN_TIMEOUT_MIN = "redis.conn.timeout.min";
        public static final String PROXY_CONN_READ_TIMEOUT_SEC = "proxy.conn.read.timeout.sec";

    }

    public static class Hive{
        public static final String JDBC_URL = "jdbcUrl";
        public static final String JDBC_USER = "jdbcUser";
        public static final String JDBC_PASSWORD = "jdbcPassword";

        public static final String SYNC_CLIENT_KEY = "sync-client";
        public static final String ASYNC_COMPUTE_CLIENT_KEY = "async-compute-client";

        public static final String HIVE_CONNECTOR_CONNECTURI="hive.connector.connectURI";
        public static final String HIVE_CONNECTOR_USER="hive.connector.user";
        public static final String HIVE_CONNECTOR_PASSWORD="hive.connector.password";
        public static final String HIVE_SQL_EXECUTE_DURATION_SEC ="hive.sql.execute.duration.sec";
        public static final String HIVE_SQL_RESULT_LIVE_SIZE ="hive.sql.result.live.size";
        public static final String HIVE_CONN_RETRY_INTERVAL_SEC="hive.conn.retry.interval.second";
        public static final String HIVE_COMPUTE_CONN_TIMEOUT_MIN="hive.conn.timeout.min";
    }

    public static class ScanQueryConstant {
        public static final int BATCH_SIZE = 10000;

        public static final int LIMIT_SIZE = 2000000;

        public static final String TIME_OUT_KEY = "timeout";

        public static final int TIME_OUT = 60 * 1000; // 1 minute
    }
}
