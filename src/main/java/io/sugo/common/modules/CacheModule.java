package io.sugo.common.modules;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.sugo.common.guice.annotations.LazySingleton;
import io.sugo.services.cache.Caches;

/**
 * Created by chenyuzhi on 18-9-25.
 */
public class CacheModule implements Module {
	@Override
	public void configure(Binder binder) {
		binder.requestStaticInjection(Caches.class);
		LifecycleModule.register(binder, Caches.class);
		binder.bind(Caches.RedisClientCache.class).in(LazySingleton.class);
		binder.bind(Caches.HiveClientCache.class).in(LazySingleton.class);
	}
}
