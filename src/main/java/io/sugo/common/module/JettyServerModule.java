package io.sugo.common.module;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import io.sugo.common.guice.annotations.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.sun.jersey.api.core.PackagesResourceConfig.PROPERTY_PACKAGES;

/**
 * Created by chenyuzhi on 18-9-6.
 */
public class JettyServerModule extends JerseyServletModule {
	private static final Logger LOG = LogManager.getLogger(JettyServerModule.class);

	@Override
	protected void configureServlets()
	{
		Map<String, String> params = new HashMap<>();
		params.put(PROPERTY_PACKAGES, "io.sugo.server.http.resource");
		//这里控制jersey注解path的过滤，和指定jersey api所在的包（可以在web.xml中配置）
		serve("/*").with(GuiceContainer.class, params);

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
