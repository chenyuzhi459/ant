package io.sugo.server.http;

import com.google.common.base.Throwables;
import com.google.inject.Injector;
import com.metamx.common.lifecycle.Lifecycle;
import io.sugo.common.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static io.sugo.common.utils.Constants.*;

/**
 * Created by chenyuzhi on 18-9-18.
 */
public class ServerRunnable implements Runnable {
	private static final Logger log = LogManager.getLogger(ServerRunnable.class);
	private Injector injector;

	public ServerRunnable(Injector injector)
	{
		this.injector = injector;
	}

	@Override
	public void run()
	{
		final Lifecycle lifecycle = initLifecycle(this.injector);

		try {
			lifecycle.join();
		}
		catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	public Lifecycle initLifecycle(Injector injector)
	{
		try {
			final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);
			int httpPort = injector.getInstance(Configure.class).getInt(Constants.SYSTEM_PROPS, Sys.HTTP_PORT);

			log.info(
					String.format("Starting up with processors[%,d], memory[%,d].",
							Runtime.getRuntime().availableProcessors(),
							Runtime.getRuntime().totalMemory())
			);

			try {
				lifecycle.start();
				log.info("Started server... in " + httpPort);
			} catch (Throwable t) {
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

}
