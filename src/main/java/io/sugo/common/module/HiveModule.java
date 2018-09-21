package io.sugo.common.module;

import com.google.inject.*;
import io.sugo.common.cache.Caches;
import io.sugo.server.hive.SQLManager;
import io.sugo.server.hive.client.HiveClient;
import io.sugo.server.hive.client.HiveClientFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by chenyuzhi on 18-9-20.
 */
public class HiveModule implements Module {
	private static final Logger log = LogManager.getLogger(HiveModule.class);
	@Override
	public void configure(Binder binder) {
		LifecycleModule.register(binder, SQLManager.class);
		binder.requestStaticInjection(HiveClientFactory.class);
		binder.requestStaticInjection(HiveClient.class);
	}

	@Provides
	@Singleton
	public Caches.HiveClientCache provideHiveClientCache(){
		return Caches.HiveClientCache.getInstance();
	}
}
