package io.sugo.common.module;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.sugo.common.guice.annotations.LazySingleton;
import io.sugo.common.guice.AntScopes;
import io.sugo.server.redis.RedisClientCache;

/**
 * Created by chenyuzhi on 18-9-6.
 */
public class CommonModule implements Module {
	@Override
	public void configure(Binder binder) {
		binder.bindScope(LazySingleton.class, AntScopes.SINGLETON);
		binder.bind(RedisClientCache.class).toInstance(RedisClientCache.getInstance());
	}
}
