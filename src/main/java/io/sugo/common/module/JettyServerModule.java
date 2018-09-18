package io.sugo.common.module;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
//import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;
import io.sugo.common.guice.annotations.Json;
import io.sugo.server.http.resource.pathanalysis.PathAnalysisResource;
import io.sugo.server.http.resource.usergroup.UserGroupResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//import static com.sun.jersey.api.core.PackagesResourceConfig.PROPERTY_PACKAGES;

/**
 * Created by chenyuzhi on 18-9-6.
 */
public class JettyServerModule extends JerseyServletModule {
	private static final Logger LOG = LogManager.getLogger(JettyServerModule.class);

	@Override
	protected void configureServlets()
	{
		Map<String, String> params = new HashMap<>();
		params.put("com.sun.jersey.config.property.packages", "io.sugo.server.http.resource");
		//这里控制jersey注解path的过滤，和指定jersey api所在的包
		serve("/*").with(GuiceContainer.class, params);

	}

	public static class PioGuiceContainer extends GuiceContainer
	{
//		private final Set<Class<?>> resources;

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
			ResourceConfig resourceConfig = new DefaultResourceConfig(resources);
//			resourceConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
			return resourceConfig;
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
