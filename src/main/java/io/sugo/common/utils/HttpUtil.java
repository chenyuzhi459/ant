package io.sugo.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.sugo.server.http.Configure;
import io.sugo.services.exception.RemoteException;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static io.sugo.common.utils.Constants.*;

/**
 * Created by chenyuzhi on 18-9-29.
 */
public class HttpUtil {
	private static final Logger log = LogManager.getLogger(JsonObjectIterator.class);
	public static final int DEFAULT_READ_TIMEOUT_SECOND = 30;

	@Inject @Named(Sys.PROXY_CONN_READ_TIMEOUT)
	private static int connReadTimeout;
	@Inject
	private static ObjectMapper jsonMapper;

	public static Response post(String url, String content) throws Exception{
		// set a long readTimeout because hproxy/query will take long time to return when data format wrong/too large.
		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(connReadTimeout, TimeUnit.SECONDS).build();
		RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), content);
		Request request = new Request.Builder().url(url).post(body).build();

		return client.newCall(request).execute();
	}

	public static List<Map> getQueryResult(String broker, String content){
		try(Response res = post(broker, content)){
			List<Map> result = new ArrayList<>();
			if(res.code() == 200){
				InputStream stream = res.body().byteStream();
				JsonObjectIterator iterator = new JsonObjectIterator(stream);

				while (iterator.hasNext()) {
					HashMap resultValue = iterator.next();
					if (resultValue != null) {
						result.add(resultValue);
					}
				}
				return result;
			}else {
				String errStr = res.body().string();
				Object originalMessage = jsonMapper.readValue(errStr, Object.class);

				throw new RemoteException(originalMessage, errStr);
			}
		}catch (Throwable t){
			log.error("GetQueryResult request error!", t);
			throw Throwables.propagate(t);
		}
	}
}
