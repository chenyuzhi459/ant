package io.sugo.services.tag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.common.utils.HttpUtil;
import io.sugo.common.utils.JsonObjectIterator;
import io.sugo.services.exception.RemoteException;
import io.sugo.services.tag.model.QueryUpdateBean;
import io.sugo.services.tag.model.UserGroupUpdateBean;
import io.sugo.services.tag.model.UpdateBatch;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
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

	public Map<String, Object> updateUserGroup(UserGroupUpdateBean userGroupUpdateBean){
		final Map<String, Object> dimData = userGroupUpdateBean.getDimData();
		final Map<String, Boolean> appendFlags = userGroupUpdateBean.getAppendFlags();
		final String primaryColumn = userGroupUpdateBean.getPrimaryColumn();
		final Set<String> userGroupData = new HashSet<>();
		final RedisDataIOFetcher userGroupDataConfig = userGroupUpdateBean.getUserGroupConfig();
		final UserGroupSerDeserializer serDeserializer = new UserGroupSerDeserializer(userGroupDataConfig);
		long before = System.currentTimeMillis();
		log.info(String.format("Begin to update data to datasource[%s] for userGroup[%s]...",
				userGroupUpdateBean.getDataSource(), userGroupDataConfig.getGroupId()));

		try {
			serDeserializer.deserialize(userGroupData);
			List<UpdateBatch> updateBatches = new ArrayList<>(userGroupData.size());
			for(String distinctId : userGroupData){
				Map<String, Object> updateValues = new HashMap<>(dimData.size() + 1);
				updateValues.put(primaryColumn, distinctId);
				updateValues.putAll(dimData);
				updateBatches.add(new UpdateBatch(updateValues, appendFlags));
			}
			Map<String, Object> result = sendData(userGroupUpdateBean.getHproxyUrl(), updateBatches);
			long after = System.currentTimeMillis();
			log.info(String.format("Update data to datasource[%s] for userGroup[%s] total cost %d ms.",
					userGroupUpdateBean.getDataSource(), userGroupDataConfig.getGroupId(), after - before));
			return result;
		}finally {
			userGroupData.clear();
		}
	}

	public Map<String, Object> updateQueryData(QueryUpdateBean queryUpdateBean) {
		Map<String, String> dimMap = queryUpdateBean.getDimMap();
		Map<String, Boolean> appendFlags = queryUpdateBean.getAppendFlags();

		log.info(String.format("Begin to update data to datasource[%s] for query...", queryUpdateBean.getDataSource()));
		long before = System.currentTimeMillis();
		List<Map> queryResult = getGroupByQueryResult(queryUpdateBean);
		List<UpdateBatch> updateBatches = new LinkedList<>();
		for(Map itemMap : queryResult){
			Map<String, Object> eventData = (Map<String, Object>)itemMap.get("event");
			Map<String, Object> convertData = new HashMap<>();
			Iterator<Map.Entry<String, String>> dimMapIter = dimMap.entrySet().iterator();
			while (dimMapIter.hasNext()){
				Map.Entry<String, String> entry = dimMapIter.next();
				String queryDim = entry.getKey();
				String updateDim = entry.getValue();
				Object dataItem = eventData.get(queryDim);
				if(dataItem == null){
					continue;
				}
				convertData.put(updateDim, dataItem);
			}

			updateBatches.add(new UpdateBatch(convertData, appendFlags));
		}
		Map<String, Object> result = sendData(queryUpdateBean.getHproxyUrl(), updateBatches);
		long after = System.currentTimeMillis();
		log.info(String.format("Update data to datasource[%s] for query, total cost %d ms.",
				queryUpdateBean.getDataSource(),  after - before));
		return result;
	}

	private List<Map> getGroupByQueryResult(QueryUpdateBean queryUpdateBean) {
		try {
			String brokerUrl = queryUpdateBean.getBrokerUrl();
			String queryStr = jsonMapper.writeValueAsString(queryUpdateBean.getQuery());
			log.info(String.format("Begin to request getGroupByQueryResult, requestMetada:\n" +
					">>>>>>>>>>>>>>>>[GroupByQuery]\n " +
					"url= %s \n param= %s\n" +
					"<<<<<<<<<<<<<<<<", brokerUrl, queryStr));

			long before = System.currentTimeMillis();
			List<Map> result = HttpUtil.getQueryResult(brokerUrl, queryStr);
			long after = System.currentTimeMillis();
			log.info(String.format("GetUserGroupQueryResult total cost %d ms.", after - before));
			return result;
		}catch (Throwable t){
			throw Throwables.propagate(t);
		}
	}

	private Map<String, Object> sendData(String url, List<UpdateBatch> updateBatches){
		Map<String, Object> result;
		try {
			String queryStr = jsonMapper.writeValueAsString(updateBatches);
			log.info(String.format("Begin to send data to url[%s], size[%s]", url, updateBatches.size()));

			long before = System.currentTimeMillis();

			try (Response response = HttpUtil.post(url, queryStr)){
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
