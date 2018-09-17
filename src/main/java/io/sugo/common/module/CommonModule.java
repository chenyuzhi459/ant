package io.sugo.common.module;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import io.sugo.common.guice.annotations.LazySingleton;
import io.sugo.common.guice.AntScopes;
import io.sugo.server.http.Configure;
import io.sugo.server.redis.RedisClientCache;

/**
 * Created by chenyuzhi on 18-9-6.
 */
public class CommonModule implements Module {
	private String configPath;

	public CommonModule(String configPath){
		this.configPath = configPath;
	}

	@Override
	public void configure(Binder binder) {
		binder.bindScope(LazySingleton.class, AntScopes.SINGLETON);
	}

	@Provides
	@Singleton
	public Configure provideConfigure(){
		Configure.initConfigPath(configPath);
		return Configure.getConfigure();
	}

	@Provides
	@Singleton
	public RedisClientCache provideRedisClientCache(Configure configure){
		return RedisClientCache.getInstance(configure);
	}
}
