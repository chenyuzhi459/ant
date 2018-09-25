package io.sugo.common.module;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.sugo.services.cache.Caches;

/**
 * Created by chenyuzhi on 18-9-25.
 */
public class CacheModule implements Module {
	@Override
	public void configure(Binder binder) {
		binder.requestStaticInjection(Caches.class);
		LifecycleModule.register(binder, Caches.class);
	}

	@Provides
	@Singleton
	public Caches.RedisClientCache provideRedisClientCache(){
		return Caches.RedisClientCache.getInstance();
	}

	@Provides
	@Singleton
	public Caches.HiveClientCache provideHiveClientCache(){
		return Caches.HiveClientCache.getInstance();
	}
}
