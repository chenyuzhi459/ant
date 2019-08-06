package io.sugo.services.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.common.utils.HttpUtil;

import io.sugo.services.tag.model.QueryUpdateBean;
import io.sugo.services.tag.model.UserGroupUpdateBean;
import io.sugo.services.tag.model.UpdateBatch;
import io.sugo.services.usergroup.parser.Parser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

/**
 * Created by chenyuzhi on 18-9-25.
 */
public class DataUpdateHelper {
	private static final Logger log = LogManager.getLogger(DataUpdateHelper.class);
	public static final int DEFAULT_BATCH_SIZE = 100;
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
		final String datasource = userGroupUpdateBean.getDataSource();
		long before = System.currentTimeMillis();
		log.info(String.format("Begin to update data to datasource[%s] for userGroup[%s]...",
				datasource, userGroupDataConfig.getGroupId()));

		try {
			serDeserializer.deserialize(userGroupData);
			List<UpdateBatch> updateBatches = new ArrayList<>(userGroupData.size());
			for(String distinctId : userGroupData){
				Map<String, Object> updateValues = new HashMap<>(dimData.size() + 1);
				updateValues.put(primaryColumn, distinctId);
				updateValues.putAll(dimData);
				updateBatches.add(new UpdateBatch(updateValues, appendFlags));
			}

			log.info(String.format("Sending data to datasource[%s], size[%s]", datasource, updateBatches.size()));
			Map<String, Object> result = updateBatches.isEmpty() ?
					ImmutableMap.of("success", 0, "failed", 0, "errors", Collections.emptyList()) :
					HttpUtil.sendData(userGroupUpdateBean.getHproxy(), datasource, updateBatches);
			long after = System.currentTimeMillis();
			log.info(String.format("Update data to datasource[%s] for userGroup[%s] total cost %d ms.",
					userGroupUpdateBean.getDataSource(), userGroupDataConfig.getGroupId(), after - before));
			return result;
		}finally {
			userGroupData.clear();
		}
	}

	public Map<String, Object> updateQueryData(List<Map> queryResult, QueryUpdateBean queryUpdateBean, String groupByDim) {
		int sendRows = 0;
		Parser parser = queryUpdateBean.getParser();
		Map<String, Boolean> appendFlags = queryUpdateBean.getAppendFlags();
		final String datasource = queryUpdateBean.getDataSource();

		log.info(String.format("Begin to update data to datasource[%s] for query...", datasource));
		long before = System.currentTimeMillis();
		long startSecs =  before/1000;
		List<UpdateBatch> updateBatches = new LinkedList<>();
		for(Map itemMap : queryResult){
			Map<String, Object> eventData = (Map<String, Object>)itemMap.get("event");
			Map<String, Object> convertData = parser.parse(eventData, groupByDim);
			updateBatches.add(new UpdateBatch(convertData, appendFlags));
			if(updateBatches.size() % DEFAULT_BATCH_SIZE == 0){
				HttpUtil.sendData(queryUpdateBean.getHproxy(), datasource, updateBatches);
				sendRows += updateBatches.size();
				updateBatches.clear();
				long currentSecs = System.currentTimeMillis()/1000;
				if(currentSecs > startSecs){
					startSecs = currentSecs;
					log.info(String.format("send data [%s] to datasource[%s] for query.", sendRows, datasource));
				}
			}
		}


		Map<String, Object> result = updateBatches.isEmpty() ?
				ImmutableMap.of("success", 0, "failed", 0, "errors", Collections.emptyList()) :
				HttpUtil.sendData(queryUpdateBean.getHproxy(), datasource, updateBatches);
		sendRows += updateBatches.size();
		updateBatches.clear();
		long after = System.currentTimeMillis();
		log.info(String.format("Update total data[%s] to datasource[%s] for query, total cost %d ms.",
				sendRows, queryUpdateBean.getDataSource(),  after - before));
		return result;
	}

}
