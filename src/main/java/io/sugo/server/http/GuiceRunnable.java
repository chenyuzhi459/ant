package io.sugo.server.http;

/**
 * Created by chenyuzhi on 18-9-18.
 */
import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.metamx.common.lifecycle.Lifecycle;
import io.sugo.common.module.JacksonModule;
import io.sugo.server.http.initialization.Initialization;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 */
public abstract class GuiceRunnable implements Runnable {
	private final Logger log;

	private Injector baseInjector;

	public GuiceRunnable(Logger log)
	{
		this.log = log;
	}

	@Inject
	public void configure(Injector injector)
	{
		this.baseInjector = injector;
	}

	protected abstract List<? extends Module> getModules();

	public Lifecycle initLifecycle(Injector injector)
	{
		try {
			final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

			log.info(
					String.format("Starting up with processors[%,d], memory[%,d].",
					Runtime.getRuntime().availableProcessors(),
					Runtime.getRuntime().totalMemory())
			);

			try {
				lifecycle.start();
			}
			catch (Throwable t) {
				log.error("Error when starting up.  Failing.",t);
				log.error(String.format("Lifecycle:[%s] start error", lifecycle.getClass().getName()));
				System.exit(1);
			}

			return lifecycle;
		}
		catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	public Injector makeInjector()
	{
		try {
			return Initialization.makeInjectorWithModules(
					this.baseInjector, getModules()
			);
		}
		catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
}
