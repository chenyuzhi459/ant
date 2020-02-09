package io.sugo.common.modules;

import com.google.inject.*;
import com.google.inject.name.Names;
import io.sugo.common.utils.Constants;
import io.sugo.common.utils.HttpClinetUtil;
import io.sugo.common.utils.QueryUtil;
import io.sugo.common.utils.StringUtil;
import io.sugo.common.guice.annotations.LazySingleton;
import io.sugo.common.guice.AntScopes;
import io.sugo.server.http.Configure;

import static io.sugo.common.utils.Constants.*;

/**
 * Created by chenyuzhi on 18-9-6.
 */
public class CommonModule implements Module {
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
		binder.requestStaticInjection(StringUtil.class);
		binder.requestStaticInjection(HttpClinetUtil.class);
		binder.requestStaticInjection(QueryUtil.class);
	}


	public void bindConstants(Binder binder){
		//绑定hive参数
		binder.bindConstant().annotatedWith(Names.named(Hive.JDBC_URL)).to(configure.getProperty(HIVE_PROPS, Hive.HIVE_CONNECTOR_CONNECTURI));
		binder.bindConstant().annotatedWith(Names.named(Hive.JDBC_USER)).to(configure.getProperty(HIVE_PROPS, Hive.HIVE_CONNECTOR_USER));
		binder.bindConstant().annotatedWith(Names.named(Hive.JDBC_PASSWORD)).to(configure.getProperty(HIVE_PROPS, Hive.HIVE_CONNECTOR_PASSWORD));

		//绑定http转发参数
		binder.bindConstant().annotatedWith(Names.named(Sys.HTTP_FORWARD_CONN_READ_TIMEOUT_SEC))
				.to(configure.getInt(SYSTEM_PROPS, Sys.HTTP_FORWARD_CONN_READ_TIMEOUT_SEC, Constants.HTTP_FORWARD_DEFAULT_READ_TIMEOUT_SEC));

		//绑定路径分析ScanQuery参数
		binder.bindConstant().annotatedWith(Names.named(Sys.PATH_ANALYSIS_SCAN_QUERY_BATCH_SIZE))
				.to(configure.getInt(SYSTEM_PROPS, Sys.PATH_ANALYSIS_SCAN_QUERY_BATCH_SIZE, Constants.SCAN_QUERY_DEFAULT_BATCH_SIZE));
		binder.bindConstant().annotatedWith(Names.named(Sys.PATH_ANALYSIS_SCAN_QUERY_LIMIT_SIZE))
				.to(configure.getInt(SYSTEM_PROPS, Sys.PATH_ANALYSIS_SCAN_QUERY_LIMIT_SIZE, Constants.SCAN_QUERY_DEFAULT_LIMIT_SIZE));
		binder.bindConstant().annotatedWith(Names.named(Sys.PATH_ANALYSIS_SCAN_QUERY_TIMEOUT_MILLIS))
				.to(configure.getInt(SYSTEM_PROPS, Sys.PATH_ANALYSIS_SCAN_QUERY_TIMEOUT_MILLIS, Constants.SCAN_QUERY_DEFAULT_TIME_OUT_MILLIS));

		//绑定用户分群redis key参数
		binder.bindConstant().annotatedWith(Names.named(UserGroup.QUERY_QUEUE_REDIS_KEY))
				.to(configure.getProperty(SYSTEM_PROPS, UserGroup.QUERY_QUEUE_REDIS_KEY, Constants.QUERY_QUEUE_DEFAULT_REDIS_KEY));

		//绑定营销模型redis key参数
		binder.bindConstant().annotatedWith(Names.named(UserGroup.MODEL_QUEUE_REDIS_KEY))
				.to(configure.getProperty(SYSTEM_PROPS, UserGroup.MODEL_QUEUE_REDIS_KEY, Constants.MODEL_QUEUE_DEFAULT_REDIS_KEY));

		binder.bindConstant().annotatedWith(Names.named(UserGroup.QUERY_RESULT_REDIS_KEY))
				.to(configure.getProperty(SYSTEM_PROPS, UserGroup.QUERY_RESULT_REDIS_KEY, Constants.QUERY_RESULT_DEFAULT_REDIS_KEY));

	}

}
