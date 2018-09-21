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



//    public static void init(String basePath){
//
//        log.info("config basePath:" + basePath);
//        log.info("logPath=====" + System.getProperty("log.path"));
//        SYSTEM_CONFIG_FILE = basePath + "/systemConfig.properties";
//
//
//        log.info("<=== start read System Config File  ===>");
//        configs.putAll(readConfigs(SYSTEM_CONFIG_FILE));
//        checkConfigs();
//
////        LOG4J_CONFIG_FILE = basePath + "/log4j2.xml";
////        Properties logProperties = readConfigs(LOG4J_CONFIG_FILE);
////        PropertyConfigurator.configure(LOG4J_CONFIG_FILE);
//
//        log.info("System Config：" + configs);
//    }
//
//    private static Properties readConfigs(String fileName) {
//
//        Properties pro = new Properties();
//
//        String proFileName =  fileName;
//        try {
//            InputStream in = new FileInputStream(proFileName);
//            if (in != null) {
//                pro.load(new InputStreamReader(in, UTF_8));
//            }
////            log.info("Configs:" + pro);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return pro;
//    }
//
//    public static Properties getAllConfigs(){
//        return configs;
//    }
//
//    public static String getAppPath() {
//        URL url = Constants.class.getProtectionDomain().getCodeSource().getLocation();
//        String filePath = null;
//        try {
//            filePath = URLDecoder.decode(url.getPath(), "utf-8");// 转化为utf-8编码
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (filePath.endsWith(".jar")) {// 可执行jar包运行的结果里包含".jar"
//            // 截取路径中的jar包名
//            filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
//        }
//
//        File file = new File(filePath);
//
//        // /If this abstract pathname is already absolute, then the pathname
//        // string is simply returned as if by the getPath method. If this
//        // abstract pathname is the empty abstract pathname then the pathname
//        // string of the current user directory, which is named by the system
//        // property user.dir, is returned.
//        filePath = file.getAbsolutePath();//得到windows下的正确路径
//        return filePath + "/";
//    }
//
//    private static void checkConfigs() {
//        //检查配置参数是否正确
//    }
//
//    public static String getString(String key) {
//        return configs.getProperty(key);
//    }
//
//    public static String getString(String key, String defaultValue) {
//        String value = configs.getProperty(key);
//        if (StringUtil.isNullOrEmpty(value)) {
//            value = defaultValue;
//        }
//        return value;
//    }
//
//    public static String[] getStrings(String key) {
//        String value = getString(key);
//        return value.split(",");
//    }
//
//    public static boolean getBoolean(String key) {
//
//        String stringValue = getString(key);
//        if (stringValue == null) {
//            return true;
//        }
//
//        stringValue = stringValue.trim();
//        if (stringValue.equals("")) {
//            return false;
//        }
//
//        return Boolean.parseBoolean(stringValue);
//
//    }
//
//    public static long getLong(String key) {
//        return Long.parseLong(getString(key));
//    }
//
//    public static int getInt(String key) {
//        return Integer.parseInt(getString(key));
//    }
//
//    public static int getInt(String key, int defaultValue) {
//        int value = 0;
//        try {
//            value = Integer.parseInt(getString(key));
//        }catch (Exception e){
//            return defaultValue;
//        }
//        return value;
//    }
//    /**
//     * @since 5.0
//     */
//    public static Integer getInteger(String key) {
//        String value = getString(key);
//        return value == null || value.equals("") ? null : Integer.parseInt(value);
//    }
//
//    /**
//     * @since 5.0
//     */
//    public static int[] getIntArray(String key) {
//        String[] values = getStrings(key);
//        return StringUtil.toInts(values);
//    }

//    public static void tryLock(String filePath) throws Exception {
//        File file = new File(filePath);
//        file.deleteOnExit();
//        @SuppressWarnings("resource")
//        RandomAccessFile raf = new RandomAccessFile(file, "rw");
//        FileChannel channel = raf.getChannel();
//        FileLock lock = channel.tryLock();
//        if (lock != null && lock.isValid()) {
//        } else {
//            throw new RuntimeException("program is already running:" + filePath + " is already locked");
//        }
//    }


//    public static void parseArgs(String[] args) {
//        Map<String, String> argmaps = StringUtil.parseArgs(PREFIX_ARG, args);
//        if (argmaps.containsKey("bindip")) {
//            configs.setProperty(API_SERVER_LISTEN_IP, argmaps.get("bindip"));
//        }
//        if (argmaps.containsKey("port")) {
//            configs.setProperty(API_SERVER_LISTEN_PORT, argmaps.get("port"));
//        }
//
//        log.info("--------- stork agent configs -----------------------");
//        log.info("系统配置参数为：" + configs);
//        log.info("--------- stork agent configs -----------------------");
//    }

}
