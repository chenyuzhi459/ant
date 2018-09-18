package io.sugo.server.http;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.metamx.common.lifecycle.Lifecycle;
import io.sugo.common.module.CommonModule;
import io.sugo.common.module.JacksonModule;
import io.sugo.common.module.LifecycleModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;

import java.util.List;

/**
 * Created by chenyuzhi on 18-9-18.
 */
public class ServerRunnable extends GuiceRunnable {
	private static final Logger log = LogManager.getLogger(ServerRunnable.class);
	private String configPath;
	@Override
	public void run()
	{
		final Injector injector = makeInjector();
		final Lifecycle lifecycle = initLifecycle(injector);

		try {
			lifecycle.join();
		}
		catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
	public ServerRunnable(String configPath)
	{
		super(log);
		this.configPath = configPath;
	}


	@Override
	protected List<? extends Module> getModules() {
		return ImmutableList.<Module>of(
				new Module() {
					@Override
					public void configure(Binder binder) {
						LifecycleModule.register(binder, Server.class);
					}
				},
				new CommonModule(configPath),
				new JacksonModule()
		);
	}
}
