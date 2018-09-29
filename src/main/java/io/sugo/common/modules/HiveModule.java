package io.sugo.common.modules;

import com.google.inject.*;
import io.sugo.services.hive.SQLManager;
import io.sugo.services.hive.client.HiveClient;
import io.sugo.services.hive.client.HiveClientFactory;
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

}
