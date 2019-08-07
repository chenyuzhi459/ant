package io.sugo.common.modules;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.sugo.common.guice.AntScopes;
import io.sugo.common.guice.annotations.LazySingleton;
import io.sugo.services.hive.SQLManager;
import io.sugo.services.usergroup.UserGroupHelper;

/**
 * Created by chenyuzhi on 19-8-7.
 */
public class UserGroupModule implements Module {
	@Override
	public void configure(Binder binder) {
		LifecycleModule.register(binder, UserGroupHelper.class);
		binder.bind(UserGroupHelper.class).in(LazySingleton.class);
	}
}
