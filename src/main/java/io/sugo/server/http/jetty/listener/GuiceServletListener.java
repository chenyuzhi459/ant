package io.sugo.server.http.jetty.listener;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import io.sugo.common.module.CommonModule;
import io.sugo.common.module.JettyServerModule;
import io.sugo.common.module.JacksonModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by chenyuzhi on 18-9-6.
 */
public class GuiceServletListener extends GuiceServletContextListener {
	private static final Logger LOG = LogManager.getLogger(GuiceServletListener.class);
	@Override
	protected Injector getInjector() {
		LOG.info("GuiceServletListener");
		return Guice.createInjector(new ServletModule(){
			@Override
			protected void configureServlets() {
				install(new CommonModule());
				install(new JettyServerModule());
				install(new JacksonModule());
			}
		});
	}
}
