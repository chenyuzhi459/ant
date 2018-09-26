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
    private static Properties configs = new Properties();

//    private static final String UTF_8 = "UTF-8";
//
//    public static final String PREFIX_ARG = "-";
//
////    private static String basePath = "src/main/resources";
//    public static  String SYSTEM_CONFIG_FILE ;
//    public static  String LOG4J_CONFIG_FILE ;
//
//    public static final String PROJECT_NAME = "hive.projectname";

    public static final String SYSTEM_PROPS = "system.properties";
    public static final String HIVE_PROPS = "hive.properties";

    public static class Sys{
//        server.max.conn=100
//        redis.conn.timeout.min=2
//        http.port=6061

        public static final String SERVER_MAX_CONN = "server.max.conn";
        public static final String HTTP_PORT = "http.port";
        public static final String REDIS_CONN_TIMEOUT_MIN = "redis.conn.timeout.min";

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

}
