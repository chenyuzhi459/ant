package io.sugo.server.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Configure {
  private static final Logger LOG = LogManager.getLogger(Configure.class);
  @Named("configPath")
  public static  String CONFIG_PATH ;

  private Map<String, Properties> allProperties = new HashMap<>();
  private Map<String, Map<String, String>> allPropertiesMap = new HashMap<>();

  private static Configure configure ;

  public static Configure getConfigure() {
      if(configure == null){
        synchronized (Configure.class){
          if(configure == null){
            configure = new Configure();
          }
        }
      }
      return configure;
  }

  public Configure reload() {
    configure = new Configure();
    return configure;
  }

  public static void initConfigPath(String basePath){
    LOG.info("config basePath:" + basePath);
    LOG.info("logPath=====" + System.getProperty("log.path"));
    CONFIG_PATH = basePath;
  }
  private Configure() {
    loadConf(new File(CONFIG_PATH));
    getAllPropertiesToMap();
  }

  private void loadConf(File file) {
    if (!file.exists()) {
      LOG.error(CONFIG_PATH + " not exists");
      return;
    }
    if (file.isDirectory()) {
      File files[] = file.listFiles((dir, name) ->{return name.endsWith(".properties");});
      LOG.info(  "reading config info");
      for (File sonFile : files) {
        if (sonFile.isFile()) {
          allProperties.put(sonFile.getName(),loadConfFromFile(sonFile));
        } else {
          loadConf(sonFile);
        }
      }
    }
  }

  private Properties loadConfFromFile(File file) {
    Properties properties = new Properties();
    try {
      LOG.info("---------------------------------------");
      properties.load(new FileInputStream(file));
      LOG.info("confName: " + file.getName());
      for (Object key : properties.keySet()) {
        LOG.info(key + " : " + properties.getProperty(key.toString()));
      }
    } catch (IOException ix) {
      ix.printStackTrace();
    } finally {
      return properties;
    }
  }

  public String getProperty(String propName,String key) {
    return getProperty(propName, key, "");
  }

  public String getProperty(String propName, String key, String defaultValue) {
    if(allProperties.containsKey(propName)) {
      return allProperties.get(propName).getProperty(key, defaultValue);
    } else {
      return "";
    }
  }

  public Properties getProperties(String propName) {
    if(allProperties.containsKey(propName)) {
      return allProperties.get(propName);
    } else {
      return null;
    }
  }

  public int getInt(String propName,String key) {
    String value = getProperty(propName,key);
    try {
      return Integer.parseInt(value);
    } catch (Exception e) {
      LOG.error("Configure getInt error!", e);
    }
    return 0;
  }

  public int getInt(String propName,String key,int defaultValue) {

    String value = getProperty(propName,key,defaultValue+"");
    try {
      return Integer.parseInt(value);
    } catch (Exception e) {
      LOG.error("Configure getInt error!", e);
    }
    return 0;
  }

  public boolean getBoolean(String propName, String key) {
    String value = getProperty(propName,key);
    try {
      return Boolean.parseBoolean(value);
    } catch (Exception e) {
      LOG.error("Configure getBoolean error!", e);
    }
    return false;
  }

  public boolean getBoolean(String propName,String key,boolean defaultValue) {

    String value = getProperty(propName,key,defaultValue+"");
    try {
      return Boolean.parseBoolean(value);
    } catch (Exception e) {
      LOG.error("Configure getBoolean error!", e);
    }
    return false;
  }

  public Map<String, Map<String, String>> getAllPropertiesToMap() {
      Iterator<String> it = allProperties.keySet().iterator();
      while(it.hasNext()) {
        String confName = it.next();
        allPropertiesMap.put(confName, propertiesToMap(allProperties.get(confName)));
      }
      return allPropertiesMap;
  }

  public Map<String, Map<String, String>> getAllPropertiesMap() {
    return this.allPropertiesMap;
  }


  public Map<String, String> propertiesToMap(Properties properties) {
    Map<String, String> propertiesMap = new HashMap<>();
    Set<String> propertyNamesSet = properties.stringPropertyNames();
    Iterator<String> it = propertyNamesSet.iterator();
    while (it.hasNext()) {
      String propertiesName = it.next();
      propertiesMap.put(propertiesName, properties.getProperty(propertiesName));
    }
    return propertiesMap;
  }

}
