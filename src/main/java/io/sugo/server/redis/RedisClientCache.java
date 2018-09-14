package io.sugo.server.redis;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

public class RedisClientCache {
//  private final static Logger logger = new Logger(RedisClientCache.class);
  private static final Logger logger = LogManager.getLogger(RedisClientCache.class);
  private static final int EXPIRE_TIME = 300;

  private final Cache<String, BlockingQueue<RedisClientWrapper>> cache;
  private final Set<String> keys = new HashSet<>();

  private static final RedisClientCache instance = new RedisClientCache();

  public static RedisClientCache getInstance() {
    return instance;
  }

  private RedisClientCache() {
    CacheBuilder builder = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(EXPIRE_TIME, TimeUnit.SECONDS)
        .expireAfterAccess(EXPIRE_TIME, TimeUnit.SECONDS)
        .removalListener(
            (RemovalNotification<String, BlockingQueue<RedisClientWrapper>> notification) -> {
              String key = notification.getKey();
              BlockingQueue<RedisClientWrapper> oldVal = notification.getValue();
              RemovalCause cause = notification.getCause();
              logger.info(String.format("redis[%s] connections[%d], removed[%s], cause:[%s]",
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
      throw new RuntimeException("", e);
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
