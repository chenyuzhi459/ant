package io.sugo.server;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.metamx.common.lifecycle.Lifecycle;
import io.sugo.common.module.CommonModule;
import io.sugo.common.module.LifecycleModule;
import io.sugo.server.http.GuiceRunnable;
import io.sugo.server.usergroup.UserGroupHelper;
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
	public ServerRunnable(String configPath)
	{
		super(log);
		this.configPath = configPath;
	}
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


	@Override
	protected List<? extends Module> getModules() {
		return ImmutableList.<Module>of(
				new Module() {
					@Override
					public void configure(Binder binder) {
//						binder.bindConstant().annotatedWith(Names.named(CliConst.SERVICE_NAME)).to(CliConst.PROCESS_NAME);
//						binder.bindConstant().annotatedWith(Names.named(CliConst.SERVICE_PORT)).to(CliConst.PROCESS_PORT);

//						JsonConfigProvider.bind(binder, "pio.broker.data.fetcher", DataFetcherConfig.class);
//
//						Jerseys.addResource(binder, RFMResource.class);
//						Jerseys.addResource(binder, PathAnalysisResource.class);
//						Jerseys.addResource(binder, UserGroupResource.class);

//						binder.bind(JettyServerInitializer.class).to(UIJettyServerInitializer.class).in(LazySingleton.class);
						LifecycleModule.register(binder, Server.class);
					}
				},
				new CommonModule(configPath)
		);
	}
}
