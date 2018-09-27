package io.sugo.services.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.common.utils.DefaultObjectMapper;
import io.sugo.services.exception.RemoteException;
import io.sugo.services.tag.model.DataBean;
import io.sugo.services.tag.model.UpdateBatch;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenyuzhi on 18-9-25.
 */
public class DataUpdateHelper {
	private static final Logger log = LogManager.getLogger(DataUpdateHelper.class);
	private final ObjectMapper jsonMapper;

	@Inject
	public DataUpdateHelper(@Json ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	public Map<String, Object>  update(DataBean dataBean){
		final Map<String, Object> dimData = dataBean.getDimData();
		final Map<String, Boolean> appendFlags = dataBean.getAppendFlags();
		final String primaryColumn = dataBean.getPrimaryColumn();
		final Set<String> userGroupData = new HashSet<>();
		final RedisDataIOFetcher userGroupDataConfig = dataBean.getUserGroupConfig();
		final UserGroupSerDeserializer serDeserializer = new UserGroupSerDeserializer(userGroupDataConfig);
		long before = System.currentTimeMillis();
		log.info(String.format("Begin to update data for userGroup[%s]...", userGroupDataConfig.getGroupId()));

		try {
			serDeserializer.deserialize(userGroupData);
			List<UpdateBatch> updateBatches = new ArrayList<>(userGroupData.size());
			for(String distinctId : userGroupData){
				Map<String, Object> updateValues = new HashMap<>(dimData.size() + 1);
				updateValues.put(primaryColumn, distinctId);
				updateValues.putAll(dimData);
				updateBatches.add(new UpdateBatch(updateValues, appendFlags));
			}
			Map<String, Object> result = sendData(dataBean.getHproxyUrl(), updateBatches);
			long after = System.currentTimeMillis();
			log.info(String.format("Update data to datasource[%s] for userGroup[%s] total cost %d ms.",
					dataBean.getDataSource(), userGroupDataConfig.getGroupId(), after - before));
			return result;
		}finally {
			userGroupData.clear();
		}


	}

	private Map<String, Object> sendData(String url, List<UpdateBatch> updateBatches){
		Map<String, Object> result;
		try {
			String queryStr = jsonMapper.writeValueAsString(updateBatches);
			log.info(String.format("Begin to send data to url[%s], size[%s]", url, updateBatches.size()));

			long before = System.currentTimeMillis();
			// set a long readTimeout because hproxy will take long time to return when data format wrong.
			OkHttpClient client = new OkHttpClient.Builder()
					.readTimeout(30, TimeUnit.SECONDS).build();
			RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), queryStr);
			Request request = new Request.Builder().url(url).post(body).build();


			try (Response response = client.newCall(request).execute()){
				if(response.code() == 200){
					String resultStr = response.body().string();
					result = this.jsonMapper.readValue(resultStr, Map.class);
					if( (Integer) result.get("failed") > 0){
						throw new RemoteException(result, resultStr);
					}
					long after = System.currentTimeMillis();
					log.info(String.format("Send data total cost %d ms.", after - before));
				}else {
					String errStr = response.body().string();
					Object originalMessage = jsonMapper.readValue(errStr, Object.class);

					throw new RemoteException(originalMessage, errStr);
				}
			}
		}catch (Exception e){
			throw Throwables.propagate(e);
		}
		return result;
	}
}
