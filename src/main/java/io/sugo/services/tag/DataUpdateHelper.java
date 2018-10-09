package io.sugo.services.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.common.utils.HttpUtil;
import io.sugo.services.tag.model.QueryUpdateBean;
import io.sugo.services.tag.model.UserGroupUpdateBean;
import io.sugo.services.tag.model.UpdateBatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

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
			Map<String, Object> result = HttpUtil.sendData(userGroupUpdateBean.getHproxy(), datasource, updateBatches);
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
		final String datasource = queryUpdateBean.getDataSource();

		log.info(String.format("Begin to update data to datasource[%s] for query...", datasource));
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
		log.info(String.format("Sending data to datasource[%s], size[%s]", datasource, updateBatches.size()));
		Map<String, Object> result = HttpUtil.sendData(queryUpdateBean.getHproxy(), datasource, updateBatches);
		long after = System.currentTimeMillis();
		log.info(String.format("Update data to datasource[%s] for query, total cost %d ms.",
				queryUpdateBean.getDataSource(),  after - before));
		return result;
	}

	private List<Map> getGroupByQueryResult(QueryUpdateBean queryUpdateBean) {
		try {
			String broker = queryUpdateBean.getBroker();
			String queryStr = jsonMapper.writeValueAsString(queryUpdateBean.getQuery());
			log.info(String.format("Begin to request GroupByQueryResult, requestMetada:\n" +
					">>>>>>>>>>>>>>>>[GroupByQuery]\n " +
					"broker= %s \n param= %s\n" +
					"<<<<<<<<<<<<<<<<", broker, queryStr));

			long before = System.currentTimeMillis();
			List<Map> result = HttpUtil.getQueryResult(broker, queryStr);
			long after = System.currentTimeMillis();
			log.info(String.format("GroupByQuery total cost %d ms.", after - before));
			return result;
		}catch (Throwable t){
			throw Throwables.propagate(t);
		}
	}
}
