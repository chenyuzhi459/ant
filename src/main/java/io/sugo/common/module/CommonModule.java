package io.sugo.common.module;

import com.google.inject.*;
import com.google.inject.name.Names;
import io.sugo.services.cache.Caches;
import io.sugo.common.guice.annotations.LazySingleton;
import io.sugo.common.guice.AntScopes;
import io.sugo.server.http.Configure;

import static io.sugo.common.utils.Constants.*;

/**
 * Created by chenyuzhi on 18-9-6.
 */
public class CommonModule implements Module {
	private String configPath;
	private Configure configure;

	public CommonModule(String configPath){
		Configure.initConfigPath(configPath);
		this.configure = Configure.getConfigure();
	}

	@Override
	public void configure(Binder binder) {
		binder.bindScope(LazySingleton.class, AntScopes.SINGLETON);
		binder.bind(Configure.class).toInstance(configure);
		bindConstants(binder);

	}


	public void bindConstants(Binder binder){
		binder.bindConstant().annotatedWith(Names.named(Hive.JDBC_URL)).to(configure.getProperty(HIVE_PROPS, Hive.HIVE_CONNECTOR_CONNECTURI));
		binder.bindConstant().annotatedWith(Names.named(Hive.JDBC_USER)).to(configure.getProperty(HIVE_PROPS, Hive.HIVE_CONNECTOR_USER));
		binder.bindConstant().annotatedWith(Names.named(Hive.JDBC_PASSWORD)).to(configure.getProperty(HIVE_PROPS, Hive.HIVE_CONNECTOR_PASSWORD));
	}

}
