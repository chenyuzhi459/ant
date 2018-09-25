package io.sugo.common.module;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.*;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceFilter;
import com.metamx.common.lifecycle.Lifecycle;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;
import io.sugo.common.cache.Caches;
import io.sugo.common.guice.KeyHolder;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.guice.annotations.LazySingleton;
import io.sugo.common.utils.AntService;
import io.sugo.server.hive.SQLManager;
import io.sugo.server.http.Configure;
import io.sugo.server.http.jetty.JettyMonitoringConnectionFactory;
import io.sugo.server.http.resource.hive.HiveClientResource;
import io.sugo.server.http.resource.pathanalysis.PathAnalysisResource;
import io.sugo.server.http.resource.usergroup.UserGroupResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class JettyServerModule extends JerseyServletModule {
	private static final Logger log = LogManager.getLogger(JettyServerModule.class);
	private static final AtomicInteger activeConnections = new AtomicInteger();

	@Override
	protected void configureServlets()
	{

		Binder binder = binder();
		//register server
		LifecycleModule.register(binder, Server.class);

		binder.bind(GuiceContainer.class).to(PioGuiceContainer.class);
		binder.bind(PioGuiceContainer.class).in(Scopes.SINGLETON);
		serve("/*").with(PioGuiceContainer.class);

	}

	public static class PioGuiceContainer extends GuiceContainer
	{
		@Inject
		public PioGuiceContainer(Injector injector) {
			super(injector);
		}

		@Override
		protected ResourceConfig getDefaultResourceConfig(
				Map<String, Object> props, WebConfig webConfig
		) throws ServletException
		{
			Set<Class<?>> resources = new HashSet<>();
			resources.add(UserGroupResource.class);
			resources.add(PathAnalysisResource.class);
			resources.add(HiveClientResource.class);
			ResourceConfig resourceConfig = new DefaultResourceConfig(resources);
			resourceConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
			return resourceConfig;
		}
	}

	@Provides
	@LazySingleton
	public Server getServer(
			final Injector injector, final Lifecycle lifecycle,final Configure configure)
	{
		final Server server = makeJettyServer(configure);
		initializeServer(injector, lifecycle, server);
		initialServices(injector, lifecycle);
		return server;
	}

	static Server makeJettyServer(Configure configure){
		int maxConn = configure.getInt("system.properties","server.max.conn", 200);
		final Server server = new Server(new QueuedThreadPool(maxConn));

		// Without this bean set, the default ScheduledExecutorScheduler runs as non-daemon, causing lifecycle hooks to fail
		// to fire on main exit. Related bug: https://github.com/druid-io/druid/pull/1627
		server.addBean(new ScheduledExecutorScheduler("JettyScheduler", true), true);
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(configure.getInt("system.properties","http.port"));
		connector.setIdleTimeout(600000);
		// workaround suggested in -
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435322#c66 for jetty half open connection issues during failovers
		connector.setAcceptorPriorityDelta(-1);

		List<ConnectionFactory> monitoredConnFactories = new ArrayList<>();
		for (ConnectionFactory cf : connector.getConnectionFactories()) {
			monitoredConnFactories.add(new JettyMonitoringConnectionFactory(cf, activeConnections));
		}
		connector.setConnectionFactories(monitoredConnFactories);

		server.setConnectors(new Connector[]{connector});

		return server;
	}

	static void initializeServer(Injector injector, Lifecycle lifecycle, final Server server)
	{
		try {
			final ServletContextHandler apiHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);

			apiHandler.addServlet(new ServletHolder(new DefaultServlet()),"/*");

			// add GuiceFilter to support Guice Inject
			apiHandler.addFilter(GuiceFilter.class,"/*", EnumSet.of(DispatcherType.REQUEST));
			HandlerList handlerList = new HandlerList();
			handlerList.addHandler(apiHandler);

			server.setHandler(handlerList);

		}
		catch (ConfigurationException e) {
			throw new ProvisionException(Iterables.getFirst(e.getErrorMessages(), null).getMessage());
		}

		lifecycle.addHandler(
				new Lifecycle.Handler()
				{
					@Override
					public void start() throws Exception
					{
						server.start();
					}

					@Override
					public void stop()
					{
						try {
							server.stop();
						}
						catch (Exception e) {
							log.warn("Unable to stop Jetty server.",e);
						}
					}
				}
		);

	}
	static void initialServices(Injector injector, Lifecycle lifecycle){
		final Key<Set<KeyHolder>> keyHolderKey = Key.get(new TypeLiteral<Set<KeyHolder>>(){}, Names.named("lifecycle"));
		// get register services in LifecycleModule
		List<Class>	serviceClasses=	injector.getInstance(keyHolderKey).stream()
				.map((keyHolder -> keyHolder.getKey().getTypeLiteral().getRawType()))
				.filter(keyClass -> AntService.class.isAssignableFrom(keyClass))
				.collect(Collectors.toList());

		for (Class<? extends AntService> serviceClass : serviceClasses) {
			AntService service = injector.getInstance(serviceClass);
			lifecycle.addHandler(
					new Lifecycle.Handler()
					{
						@Override
						public void start() throws Exception
						{
							service.start();
						}

						@Override
						public void stop()
						{
							try {
								service.stop();
							}
							catch (Exception e) {
								log.warn("Unable to stop Jetty server.",e);
							}
						}
					}
			);
		}
	}

	@Provides
	@Singleton
	public JacksonJsonProvider getJacksonJsonProvider(@Json ObjectMapper objectMapper)
	{
		final JacksonJsonProvider provider = new JacksonJsonProvider();
		provider.setMapper(objectMapper);
		return provider;
	}

}
