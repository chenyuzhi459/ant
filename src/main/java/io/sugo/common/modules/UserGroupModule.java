package io.sugo.common.modules;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.sugo.common.guice.annotations.LazySingleton;
import io.sugo.services.cache.Caches;
import io.sugo.services.usergroup.UserGroupHelper;
import io.sugo.services.usergroup.model.ModelManager;

/**
 * Created by chenyuzhi on 19-8-7.
 */
public class UserGroupModule implements Module {
	@Override
	public void configure(Binder binder) {
		//用户分群初始化注入
		binder.requestStaticInjection(UserGroupHelper.class);
		LifecycleModule.register(binder, UserGroupHelper.class);
		binder.bind(UserGroupHelper.class).in(LazySingleton.class);

		//营销分群初始化注入
		binder.requestStaticInjection(ModelManager.class);
		LifecycleModule.register(binder, ModelManager.class);
		binder.bind(ModelManager.class).in(LazySingleton.class);
	}
}
