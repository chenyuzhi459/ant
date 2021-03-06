package io.sugo.common.redis;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.SafeEncoder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by janpychou on 上午11:45.
 * Mail: janpychou@qq.com
 */
public class RedisClientWrapper{
  private static final Logger log = LogManager.getLogger(RedisClientWrapper.class);
  private Jedis jedis;
  private JedisCluster cluster;
  private JedisSentinelPool sentinelPool;

  private final boolean clusterMode;
  private final boolean sentinelMode;

  public RedisClientWrapper(RedisInfo redisInfo) {
    this.clusterMode = redisInfo.isClusterMode();
    this.sentinelMode = redisInfo.isSentinelMode();
    if (this.clusterMode) {
      cluster = new JedisCluster(redisInfo.getNodes(), 10000, 10);
    } else if (this.sentinelMode) {
      Set<String> sentinels = redisInfo.getNodes().stream().map(node -> node.toString()).collect(Collectors.toSet());
      if (StringUtils.isNotEmpty(redisInfo.getPassword())) {
        sentinelPool = new JedisSentinelPool(redisInfo.getMasterName(), sentinels, redisInfo.getPassword());
      } else {
        sentinelPool = new JedisSentinelPool(redisInfo.getMasterName(), sentinels);
      }
    } else {
      HostAndPort node = redisInfo.getNodes().iterator().next();
      jedis = new Jedis(node.getHost(), node.getPort(), 10000, 10000);
      if (StringUtils.isNotEmpty(redisInfo.getPassword())) {
        try {
          jedis.auth(redisInfo.getPassword());
        } catch (JedisDataException jde) {
          String msg = String.format("redis server[%s:%d] has no password, password is not needed", node.getHost(), node.getPort());
          log.warn(msg, jde);
        }
      }
    }
  }

  public Long rpush(String listKey, byte[] dest) {
    if (clusterMode) {
      return cluster.rpush(SafeEncoder.encode(listKey), dest);
    } else if (sentinelMode) {
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.rpush(SafeEncoder.encode(listKey), dest);
      }
    } else {
      return jedis.rpush(SafeEncoder.encode(listKey), dest);
    }
  }

  public String rpop(String key) {
    if (clusterMode) {
      return cluster.rpop(key);
    } else if (sentinelMode) {
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.rpop(key);
      }
    } else {
      return jedis.rpop(key);
    }
  }

  public Long lpush(String key, String value) {
    return this.lpush(key,new String[]{value});
  }

  public Long lpush(String key, String... values) {
    if (clusterMode) {
      return cluster.lpush(key,values);
    } else if (sentinelMode) {
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.lpush(key,values);
      }
    } else {
      return jedis.lpush(key,values);
    }
  }

  public Long llen(String listKey) {
    if (clusterMode) {
      return cluster.llen(listKey);
    } else if(sentinelMode){
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.llen(listKey);
      }
    } else {
      return jedis.llen(listKey);
    }
  }

  public byte[] lindex(String listKey, long idx) {
    if (clusterMode) {
      return cluster.lindex(SafeEncoder.encode(listKey), idx);
    } else if(sentinelMode){
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.lindex(SafeEncoder.encode(listKey), idx);
      }
    } else {
      return jedis.lindex(SafeEncoder.encode(listKey), idx);
    }
  }

  public Long hset(String key, String field, String val) {
    if (clusterMode) {
      return cluster.hset(key,field,val);
    } else if (sentinelMode) {
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.hset(key,field,val);
      }
    } else {
      return jedis.hset(key,field,val);
    }
  }

  public String hget(String key, String field ) {
    if (clusterMode) {
      return cluster.hget(key,field);
    } else if (sentinelMode) {
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.hget(key,field);
      }
    } else {
      return jedis.hget(key,field);
    }
  }

  public Map<String,String> hgetAll(String key ) {
    if (clusterMode) {
      return cluster.hgetAll(key);
    } else if (sentinelMode) {
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.hgetAll(key);
      }
    } else {
      return jedis.hgetAll(key);
    }
  }

  public Long hdel(String key, String... field ) {
    if (clusterMode) {
      return cluster.hdel(key,field);
    } else if (sentinelMode) {
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.hdel(key,field);
      }
    } else {
      return jedis.hdel(key,field);
    }
  }

  public Long del(String listKey) {
    if (clusterMode) {
      return cluster.del(listKey);
    } else if(sentinelMode){
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.del(listKey);
      }
    } else {
      return jedis.del(listKey);
    }
  }

  public Long del(String... listKey) {
    if (clusterMode) {
      return cluster.del(listKey);
    } else if(sentinelMode){
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.del(listKey);
      }
    } else {
      return jedis.del(listKey);
    }
  }

  public boolean exists(String listKey){
    if (clusterMode) {
      return cluster.exists(SafeEncoder.encode(listKey));
    } else if(sentinelMode){
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.exists(SafeEncoder.encode(listKey));
      }
    } else {
      return jedis.exists(SafeEncoder.encode(listKey));
    }
  }

  public String rename(String oldkey, String newkey){
    if (clusterMode) {
      return cluster.rename(SafeEncoder.encode(oldkey),SafeEncoder.encode(newkey));
    } else if(sentinelMode){
      try (Jedis resource = sentinelPool.getResource()) {
        return resource.rename(SafeEncoder.encode(oldkey),SafeEncoder.encode(newkey));
      }
    } else {
      return jedis.rename(SafeEncoder.encode(oldkey),SafeEncoder.encode(newkey));
    }
  }

  public void close() {
    if (clusterMode) {
      if (cluster != null) {
        try {
          cluster.close();
        } catch (IOException e) {
          throw new RuntimeException("", e);
        }
      }
    } else if(sentinelMode){
      if(sentinelPool != null) {
        sentinelPool.close();
      }
    } else {
      if (jedis != null) {
        jedis.close();
      }
    }
  }
}
