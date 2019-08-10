package io.sugo.services.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.common.utils.HttpUtil;

import io.sugo.services.usergroup.OperationResult;
import io.sugo.services.usergroup.UpdateSpec;
import io.sugo.services.tag.model.UserGroupUpdateBean;
import io.sugo.services.tag.model.UpdateBatch;
import io.sugo.services.usergroup.UserGroupHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

import static io.sugo.services.usergroup.UserGroupHelper.RESULT_REDIS_KEY;

/**
 * Created by chenyuzhi on 18-9-25.
 */
public class DataUpdateHelper {
	private static final Logger log = LogManager.getLogger(DataUpdateHelper.class);
	public static final int DEFAULT_BATCH_SIZE = 1000;
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

	public static Map<String, Integer> updateQueryData(List<Map> updateRecords, UpdateSpec updateSpec,
													  String operationId, UserGroupHelper userGroupHelper) throws IOException {
		Map<String,Integer> res = new HashMap<>(2);
		int sendRows = 0;
		Map<String, Boolean> appendFlags = updateSpec.getAppendFlags();
		final String datasource = updateSpec.getDataSource();

		log.info(String.format("Begin to update data to datasource[%s] for query...", datasource));
		OperationResult operationResult = userGroupHelper.getOperationResult(RESULT_REDIS_KEY, operationId);
		operationResult.setTotalRows(updateRecords.size()).setUpdatedRows(0);
		userGroupHelper.updateOperationResult(RESULT_REDIS_KEY, operationResult);
		if(updateRecords.isEmpty()){return res;}

		long before = System.currentTimeMillis();
		long startSecs =  before/1000;
		List<UpdateBatch> updateBatches = new LinkedList<>();
		for(Map itemMap : updateRecords){

			updateBatches.add(new UpdateBatch(itemMap, appendFlags));
			if(updateBatches.size() % DEFAULT_BATCH_SIZE == 0){
				Map<String, Object> sendRes = HttpUtil.sendData(updateSpec.getHproxy(), datasource, updateBatches);
				//sendRes -format:
				//ImmutableMap.of("success", 0, "failed", 0, "errors", Collections.emptyList())
				sendRows += (Integer) sendRes.get("success");
				updateBatches.clear();
				operationResult.setUpdatedRows(sendRows);
				userGroupHelper.updateOperationResult(RESULT_REDIS_KEY, operationResult);

				long currentSecs = System.currentTimeMillis()/1000;
				if(currentSecs > startSecs){
					startSecs = currentSecs;
					log.info(String.format("send data [%s] to datasource[%s] for query.", sendRows, datasource));
				}
			}
		}


		if(!updateBatches.isEmpty()){
			Map<String, Object> sendRes =
					HttpUtil.sendData(updateSpec.getHproxy(), datasource, updateBatches);
			sendRows += (Integer) sendRes.get("success");
			updateBatches.clear();
			operationResult.setUpdatedRows(sendRows);
			userGroupHelper.updateOperationResult(RESULT_REDIS_KEY, operationResult);
		}

		long after = System.currentTimeMillis();
		log.info(String.format("Update total data[%s] to datasource[%s] for query, total cost %d ms.",
				sendRows, updateSpec.getDataSource(),  after - before));
		return res;
	}


}
