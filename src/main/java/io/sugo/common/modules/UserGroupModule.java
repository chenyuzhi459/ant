package io.sugo.common.modules;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.sugo.common.guice.annotations.LazySingleton;
import io.sugo.services.cache.Caches;
import io.sugo.services.usergroup.UserGroupHelper;

/**
 * Created by chenyuzhi on 19-8-7.
 */
public class UserGroupModule implements Module {
	@Override
	public void configure(Binder binder) {
		binder.requestStaticInjection(UserGroupHelper.class);
		LifecycleModule.register(binder, UserGroupHelper.class);
		binder.bind(UserGroupHelper.class).in(LazySingleton.class);
	}
}
