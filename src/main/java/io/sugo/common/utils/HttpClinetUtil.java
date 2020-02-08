package io.sugo.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import io.sugo.services.exception.RemoteException;
import io.sugo.services.tag.model.UpdateBatch;
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
public class HttpClinetUtil {
	private static final Logger log = LogManager.getLogger(HttpClinetUtil.class);

	@Inject @Named(Sys.HTTP_FORWARD_CONN_READ_TIMEOUT_SEC)
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
		String[] brokers = broker.split(",");
		for(String brokerNode : brokers){
			String queryUrl = String.format("http://%s/druid/v2?pretty", brokerNode.trim());
			try(Response res = post(queryUrl, content)){
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
					Object originalMessage = errStr.isEmpty() ? errStr : jsonMapper.readValue(errStr, Object.class);

					throw new RemoteException(originalMessage, errStr);
				}
			}catch (Throwable t){
				String message = t.toString();
				if(isConnectError(message)){
					log.warn(String.format("Connect to brokerNode[%s] error.", brokerNode), t);
				}else {
					log.error(String.format("GetQueryResult error, queryUrl = %s", queryUrl));
					throw Throwables.propagate(t);
				}
			}
		}

		throw new RuntimeException("Failed to query with broker:" + broker);
	}

	public static Map<String, Object> sendData(String hproxy, String datasource, List<UpdateBatch> updateBatches){

		String[] hproxys = hproxy.split(",");
		for(String hproxyNode : hproxys){
			String url = String.format("http://%s/druid/proxy/batchupdate/%s", hproxyNode.trim(), datasource);
			String updateStr = null;
			try {
				updateStr = jsonMapper.writeValueAsString(updateBatches);

				try (Response response = HttpClinetUtil.post(url, updateStr)){
					if(response.code() == 200){
						String resultStr = response.body().string();
						Map<String, Object> result = jsonMapper.readValue(resultStr, Map.class);
						if( (Integer) result.get("failed") > 0){
							throw new RemoteException(result, resultStr);
						}
						return result;
					}else {
						String errStr = response.body().string();
						Object originalMessage = errStr.isEmpty() ? errStr : jsonMapper.readValue(errStr, Object.class);

						throw new RemoteException(originalMessage, errStr);
					}
				}
			}catch (Throwable t){
				String message = t.toString();
				if(isConnectError(message)){
					log.warn(String.format("Connect to hproxyNode[%s] error.", hproxyNode), t);
				}else {
					log.error(String.format("Send data error, url = %s", url));
					throw Throwables.propagate(t);
				}
			}
		}

		throw new RuntimeException("Failed to sendData with hproxy:" + hproxy);
	}

	private static boolean isConnectError(String message){
		return message.contains("ConnectException") ||
				message.contains("SocketException") ||
				message.contains("HostException") ||
				message.contains("Timeout");
	}
}
