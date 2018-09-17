package io.sugo.server.redis;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import io.sugo.server.http.Configure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

public class RedisClientCache {
  private static final Logger logger = LogManager.getLogger(RedisClientCache.class);
  private static int DEFAULT_EXPIRE_MINUTE = 5;

  private final Cache<String, BlockingQueue<RedisClientWrapper>> cache;
  private final Set<String> keys = new HashSet<>();

  private static  RedisClientCache instance;

  public static RedisClientCache getInstance(Configure configure) {
    int expireMin = configure.getInt("system.properties","redis.conn.timeout.min", DEFAULT_EXPIRE_MINUTE);
    instance = new RedisClientCache(expireMin);
    return instance;
  }

  private RedisClientCache(int expireMin) {
    CacheBuilder builder = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(expireMin, TimeUnit.MINUTES)
        .expireAfterAccess(expireMin, TimeUnit.MINUTES)
        .removalListener(
            (RemovalNotification<String, BlockingQueue<RedisClientWrapper>> notification) -> {
              String key = notification.getKey();
              BlockingQueue<RedisClientWrapper> oldVal = notification.getValue();
              RemovalCause cause = notification.getCause();
              logger.info(String.format("Redis[%s] connections[%d], removed[%s], cause:[%s]",
                  key, oldVal.size(), notification.wasEvicted(), cause));

              if (oldVal.size() > 0) {
                RedisClientWrapper wrapper;
                while ((wrapper = oldVal.poll()) != null) {
                  wrapper.close();
                }
              }
            }
        );

    cache = builder.build();

    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
      Iterator<String> iter = keys.iterator();
      String key;
      while (iter.hasNext()) {
        key = iter.next();
        if (cache.getIfPresent(key) == null) {
          cache.invalidate(key);
          iter.remove();
        }
      }
    }, 60, 60, TimeUnit.SECONDS);
  }

  public void releaseRedisClient(RedisInfo redisInfo, RedisClientWrapper wrapper) {
    try {
      keys.add(redisInfo.getClientStr());
      BlockingQueue<RedisClientWrapper> queue = cache.get(redisInfo.getClientStr(), () -> new LinkedBlockingQueue<>());
      queue.put(wrapper);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException("ReleaseRedisClient error", e);
    }
  }

  public RedisClientWrapper getRedisClient(RedisInfo redisInfo) {
    RedisClientWrapper wrapper;
    BlockingQueue<RedisClientWrapper> queue;
    try {
      keys.add(redisInfo.getClientStr());
      queue = cache.get(redisInfo.getClientStr(), () -> new LinkedBlockingQueue<>());
    } catch (ExecutionException e) {
      throw new RuntimeException("", e);
    }

    wrapper = queue.poll();
    if (wrapper == null) {
      wrapper = new RedisClientWrapper(redisInfo);
    }

    return wrapper;
  }
}
