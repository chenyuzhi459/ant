package io.sugo.services.cache;

import com.google.common.cache.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import io.sugo.common.utils.AntService;
import io.sugo.services.hive.client.HiveClient;
import io.sugo.services.hive.client.HiveClientFactory;
import io.sugo.server.http.Configure;
import io.sugo.common.redis.RedisClientWrapper;
import io.sugo.common.redis.RedisInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.concurrent.*;
import static io.sugo.common.utils.Constants.*;

/**
 * Created by chenyuzhi on 17-10-23.
 */
public class Caches implements AntService{
	private static final Logger log = LogManager.getLogger(Caches.class);
	private static int DEFAULT_EXPIRE_MINUTE = 5;
	private static Map<Cache, Set<String>> cachedKeysMap = new HashMap<>();
	@Inject
	private static Configure configure;
	private static volatile boolean startMonitorCache = false;
	private ScheduledExecutorService executorService;



	public static class HiveClientCache {
		private static final Logger log = LogManager.getLogger(HiveClientCache.class);
		private final Cache<String, BlockingQueue<HiveClient>> cache;

		@Inject
		private HiveClientCache() {
			int expireMin = configure.getInt(HIVE_PROPS, Hive.HIVE_COMPUTE_CONN_TIMEOUT_MIN, DEFAULT_EXPIRE_MINUTE);
			log.info("HiveClientCache initing...");
			CacheBuilder builder = CacheBuilder.newBuilder()
//					.maximumSize(5)
					.recordStats()
					.expireAfterWrite(expireMin, TimeUnit.MINUTES)
					.expireAfterAccess(expireMin, TimeUnit.MINUTES)
					.removalListener((RemovalNotification<String, BlockingQueue<HiveClient>> notification) -> {
						String key = notification.getKey();
						BlockingQueue<HiveClient> oldVal = notification.getValue();
						RemovalCause cause = notification.getCause();
						log.info(String.format("Hive[%s] connections[%d], removed[%s], cause:[%s]",
								key, oldVal.size(), notification.wasEvicted(), cause));

						if (oldVal.size() > 0) {
							HiveClient hiveClient;
							while ((hiveClient = oldVal.poll()) != null) {
								hiveClient.close();
							}
						}
					});
			cache = builder.build();
			log.info("HiveClientCache has inited successfully.");

		}

		public void releaseHiveClient(final String key, HiveClient hiveClient) {
			if(hiveClient == null){
				return;
			}
			try {
				cachedKeysMap.computeIfAbsent(cache, (cache1) ->new HashSet<>()).add(key);
				BlockingQueue<HiveClient> queue = cache.get(key, () ->{
					BlockingQueue<HiveClient> hiveClients = new LinkedBlockingQueue<>(1);
					hiveClients.put( HiveClientFactory.newClient());
					return hiveClients;
				});
				queue.put(hiveClient);
			} catch (ExecutionException | InterruptedException e) {
				throw new RuntimeException("ReleaseHiveClient error", e);
			}
		}

		public HiveClient getHiveClient(final String key) throws InterruptedException {
			HiveClient hiveClient;
			BlockingQueue<HiveClient> queue;
			try {
				cachedKeysMap.computeIfAbsent(cache, (cache1) -> new HashSet<>()).add(key);
				queue = cache.get(key, () -> {
					BlockingQueue<HiveClient> hiveClients = new LinkedBlockingQueue<>(1);
					hiveClients.put( HiveClientFactory.newClient());
					log.info(String.format("Create a cache hiveClient with key[%s].", key));
					return hiveClients;
				});
			} catch (ExecutionException e) {
				throw new RuntimeException("GetHiveClient error", e);
			}

			hiveClient = queue.take();

			return hiveClient;
		}
	}

	public static class RedisClientCache {
		private static final Logger log = LogManager.getLogger(RedisClientCache.class);
		private final Cache<String, BlockingQueue<RedisClientWrapper>> cache;

		@Inject
		private RedisClientCache() {
			int expireMin = configure.getInt(SYSTEM_PROPS, Sys.REDIS_CONN_TIMEOUT_MIN, DEFAULT_EXPIRE_MINUTE);
			log.info("RedisClientCache initing...");
			CacheBuilder builder = CacheBuilder.newBuilder()
					.recordStats()
					.expireAfterWrite(expireMin, TimeUnit.MINUTES)
					.expireAfterAccess(expireMin, TimeUnit.MINUTES)
					.removalListener(
							(RemovalNotification<String, BlockingQueue<RedisClientWrapper>> notification) -> {
								String key = notification.getKey();
								BlockingQueue<RedisClientWrapper> oldVal = notification.getValue();
								RemovalCause cause = notification.getCause();
								log.info(String.format("Redis[%s] connections[%d], removed[%s], cause:[%s]",
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
			log.info("RedisClientCache has inited successfully.");

		}

		public void releaseRedisClient(RedisInfo redisInfo, RedisClientWrapper wrapper) {
			if(wrapper == null){
				return;
			}
			try {
				String key = redisInfo.getClientStr();
				cachedKeysMap.computeIfAbsent(cache, (cache1) ->new HashSet<>()).add(key);
				BlockingQueue<RedisClientWrapper> queue = cache.get(key, () -> new LinkedBlockingQueue<>());
				queue.put(wrapper);
			} catch (ExecutionException | InterruptedException e) {
				throw new RuntimeException("ReleaseRedisClient error", e);
			}
		}

		public RedisClientWrapper getRedisClient(RedisInfo redisInfo) {
			RedisClientWrapper wrapper;
			BlockingQueue<RedisClientWrapper> queue;
			try {
				String key = redisInfo.getClientStr();
				cachedKeysMap.computeIfAbsent(cache, (cache1) -> new HashSet<>()).add(key);
				queue = cache.get(key, () -> new LinkedBlockingQueue<>());
			} catch (ExecutionException e) {
				throw new RuntimeException("GetRedisClient error", e);
			}

			wrapper = queue.poll();
			if (wrapper == null) {
				wrapper = new RedisClientWrapper(redisInfo);
			}

			return wrapper;
		}

		public long delete(RedisInfo redisInfo, String userGroupKeys){
			RedisClientWrapper redisClient = getRedisClient(redisInfo);
			Long result = redisClient.del(userGroupKeys);
			releaseRedisClient(redisInfo, redisClient);
			return result;
		}
	}


	@Override
	public void start(){
		if(startMonitorCache){
			return;
		}
		executorService = Executors.newSingleThreadScheduledExecutor(
				new ThreadFactoryBuilder().setDaemon(true)
						.setNameFormat("Cache-Monitor-Service-%s").build()
		);
		executorService.scheduleAtFixedRate(() -> {
			Iterator<Map.Entry<Cache,Set<String>>> iter = cachedKeysMap.entrySet().iterator();
			Map.Entry<Cache,Set<String>> entry;
			while (iter.hasNext()) {
				entry = iter.next();
				Cache cache = entry.getKey();
				Iterator<String> keyIterator = entry.getValue().iterator();
				while (keyIterator.hasNext()) {
					String key = keyIterator.next();
					if (cache.getIfPresent(key) == null) {
						cache.invalidate(key);
						keyIterator.remove();
					}
				}
			}
		}, 10, 30, TimeUnit.SECONDS);
		startMonitorCache = true;
		log.info("Started cache monitor service.");
	}

	@Override
	public void stop(){
		if(!startMonitorCache){
			return;
		}
		cachedKeysMap.keySet().forEach(Cache::cleanUp);
		executorService.shutdown();
		startMonitorCache = false;
		log.info("Stopped cache monitor service.");
	}
}
