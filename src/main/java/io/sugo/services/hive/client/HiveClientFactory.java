package io.sugo.services.hive.client;

import com.google.inject.Inject;
import io.sugo.server.http.Configure;
import org.apache.log4j.Logger;

/**
 * Created by chenyuzhi on 18-5-9.
 */
public class HiveClientFactory {
	private static final Logger log = Logger.getLogger(HiveClientFactory.class);
	@Inject
	private static Configure configure;

	public static HiveClient newClient(){
		return new HiveClient(configure);
	}
}
