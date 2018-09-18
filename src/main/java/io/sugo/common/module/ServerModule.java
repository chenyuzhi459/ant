package io.sugo.common.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.metamx.common.concurrent.ScheduledExecutorFactory;
import com.metamx.common.concurrent.ScheduledExecutors;
import com.metamx.common.lifecycle.Lifecycle;
import io.sugo.common.guice.annotations.LazySingleton;

import java.util.Arrays;
import java.util.List;

/**
 * Created by chenyuzhi on 18-9-18.
 */
public class ServerModule implements AntModule {
	@Override
	public void configure(Binder binder)
	{

	}
	@Provides
	@LazySingleton
	public ScheduledExecutorFactory getScheduledExecutorFactory(Lifecycle lifecycle)
	{
		return ScheduledExecutors.createFactory(lifecycle);
	}

	@Override
	public List<? extends com.fasterxml.jackson.databind.Module> getJacksonModules()
	{
		return Arrays.asList(
				new SimpleModule()
		);
	}
}
