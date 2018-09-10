package io.sugo.common.module;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.sugo.server.guice.LazySingleton;
import io.sugo.server.guice.PioScopes;

/**
 * Created by chenyuzhi on 18-9-6.
 */
public class CommonModule implements Module {
	@Override
	public void configure(Binder binder) {
		binder.bindScope(LazySingleton.class, PioScopes.SINGLETON);
	}
}
