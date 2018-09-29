package io.sugo.services.usergroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.sugo.common.utils.HttpUtil;
import io.sugo.server.http.resource.UserGroupResource;
import io.sugo.services.cache.Caches;
import io.sugo.common.utils.JsonObjectIterator;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.common.redis.RedisClientWrapper;
import io.sugo.common.redis.RedisInfo;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.services.exception.RemoteException;
import io.sugo.services.usergroup.model.UserGroupBean;
import io.sugo.services.usergroup.model.UserGroupQuery;
import io.sugo.common.guice.annotations.Json;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.InputStream;
import java.util.*;

public class UserGroupHelper {
	private static final Logger log = LogManager.getLogger(UserGroupHelper.class);
	private final ObjectMapper jsonMapper;
	private final Caches.RedisClientCache redisClientCache;
	private static final String AND_OPERATION = "and";
	private static final String OR_OPERATION = "or";
	private static final String EXCLUDE_OPERATION = "exclude";

	@Inject
	public UserGroupHelper(@Json ObjectMapper jsonMapper, Caches.RedisClientCache redisClientCache) {
		this.jsonMapper = jsonMapper;
		this.redisClientCache = redisClientCache;
	}

	public List<Map> getUserGroupQueryResult(UserGroupQuery query, String brokerUrl) {
		try {
			String queryStr = jsonMapper.writeValueAsString(query);
			log.info(String.format("Begin to request getUserGroupQueryResult, requestMetada:\n" +
					">>>>>>>>>>>>>>>>[UserGroupQuery]\n " +
					"url= %s \n param= %s\n" +
					"<<<<<<<<<<<<<<<<", brokerUrl, queryStr));

			long before = System.currentTimeMillis();

			List<Map> queryResult = HttpUtil.getQueryResult(brokerUrl, queryStr);
			Map queryResultMap = queryResult.get(0);
			//prune queryResultMap
			queryResultMap.remove("v");
			queryResultMap.computeIfPresent("event", (key, value)->{
				Map<String, Object> eventMap = (Map)value;
				return Maps.filterKeys(eventMap, (key2-> key2.equals("RowCount")));
			});

			long after = System.currentTimeMillis();
			log.info(String.format("GetUserGroupQueryResult total cost %d ms.", after - before));
			return queryResult;
		}catch (Throwable t){
			throw Throwables.propagate(t);
		}
	}

	public List<Map> doUserGroupQueryIncremental(UserGroupQuery query, String brokerUrl) {
		List<Map> result;
		log.info("Begin to doUserGroupQueryIncremental...");
		long startMillis = System.currentTimeMillis();
		RedisDataIOFetcher redisIOFactory = query.getDataConfig();
		RedisClientWrapper redisClient = redisClientCache.getRedisClient(redisIOFactory.getRedisInfo());
		String redisKey = redisIOFactory.getGroupId();
		Set<String> currentData = new HashSet<>();
		Set<String> backupData = new HashSet<>();
		String backupKey = generateRedisBackUpKey(redisKey);
		boolean backup = backupRedisData(redisClient, redisKey, backupKey);
		try {
			result = getUserGroupQueryResult(query, brokerUrl);
			if(backup){
				UserGroupSerDeserializer serDeserializer = new UserGroupSerDeserializer(redisIOFactory);
				UserGroupSerDeserializer backupSerDeserializer = new UserGroupSerDeserializer(redisIOFactory.clone(backupKey));

				serDeserializer.deserialize(currentData);
				backupSerDeserializer.deserialize(backupData);
				doDataOperation(OR_OPERATION, currentData, backupData);
				int finalLen = writeDataToRedis(serDeserializer, currentData);
				// 成功更新后, 立刻设backup = false, 避免出现异常后被backupData覆盖.
				backup = false;
				redisClient.del(backupKey);
				backupData.clear();
				currentData.clear();

				result = Collections.singletonList(ImmutableMap.of("event",  ImmutableMap.of("RowCount", finalLen)));
				long endMillis = System.currentTimeMillis();
				log.info(String.format("UserGroupQueryIncremental total cost %d ms.", endMillis - startMillis));
			}
		} finally {
			currentData.clear();
			backupData.clear();

			if(redisClient != null){
				if(backup){
					redisClient.rename(backupKey, redisKey);
				}
				redisClientCache.releaseRedisClient(redisIOFactory.getRedisInfo(), redisClient);
			}
		}
		return result;
	}

	public List<Map> doMultiUserGroupOperation(Map<String, List<UserGroupBean>> userGroupParams){
		List<Map> result;
		log.info("Begin to doMultiUserGroupOperation...");
		long startMillis = System.currentTimeMillis();
		UserGroupBean finalGroup = userGroupParams.get("finalGroup").get(0);
		List<UserGroupBean> assistantGroupList = userGroupParams.get("AssistantGroupList");

		RedisDataIOFetcher finalUserGroupDataConfig = finalGroup.getQuery().getDataConfig();
		String finalUserGroupKey = finalUserGroupDataConfig.getGroupId();
		String finalUserGroupBackupKey = generateRedisBackUpKey(finalUserGroupKey);
		RedisClientWrapper finalUserGroupRedisClient = redisClientCache.getRedisClient(finalUserGroupDataConfig.getRedisInfo());
		Map<RedisInfo, Set<String>> tempUserGroupMap = new HashMap<>();
		Set<String> acculatedData = new HashSet<>();
		Set<String> currentData = new HashSet<>();
		boolean backup = false;
		try {
			if(assistantGroupList== null || assistantGroupList.isEmpty()){
				return Collections.singletonList(ImmutableMap.of("error", "AssistantGroup array is empty"));
			}

			UserGroupSerDeserializer itemSerDeserializer;

			for(int i = 0; i < assistantGroupList.size(); i++){
				UserGroupBean userGroupBean = assistantGroupList.get(i);

				String op = userGroupBean.getOp();
				UserGroupQuery query = userGroupBean.getQuery();
				boolean isTempUserGroup =UserGroupBean.INDEX_TYPES.contains(userGroupBean.getType());
				if(isTempUserGroup){
					getUserGroupQueryResult(query, userGroupBean.getBrokerUrl());
					tempUserGroupMap.computeIfAbsent(query.getDataConfig().getRedisInfo(), k -> new HashSet<>())
							.add(query.getDataConfig().getGroupId());
				}

				itemSerDeserializer = new UserGroupSerDeserializer(query.getDataConfig());
				if(i == 0){
					itemSerDeserializer.deserialize(acculatedData);
					continue;
				}

				if(!op.isEmpty()){
					itemSerDeserializer.deserialize(currentData);
					acculatedData = doDataOperation(op, acculatedData, currentData);
				}
				currentData.clear();
			}

			UserGroupSerDeserializer finalSerDeserializer = new UserGroupSerDeserializer(finalUserGroupDataConfig);

			if(finalGroup.isAppend()){
				// do 'append operation'
				finalSerDeserializer.deserialize(currentData);
				acculatedData = doDataOperation(OR_OPERATION, acculatedData, currentData);
				currentData.clear();
			}
			// do 'backup orperation'
			backup = backupRedisData(finalUserGroupRedisClient, finalUserGroupKey, finalUserGroupBackupKey);
			int finalLen = writeDataToRedis(finalSerDeserializer, acculatedData);
			backup = false;
			finalUserGroupRedisClient.del(finalUserGroupBackupKey);
			acculatedData.clear();

			result = Collections.singletonList(ImmutableMap.of("event",  ImmutableMap.of("RowCount", finalLen)));
			long endMillis = System.currentTimeMillis();
			log.info(String.format("MultiUserGroupOperation total cost %d ms.", endMillis - startMillis));
		}finally {
			acculatedData.clear();
			currentData.clear();
			tempUserGroupMap.forEach((redisInfo, userGroupKeys) ->{
				deleteUserGroups(redisInfo, userGroupKeys.toArray(new String[userGroupKeys.size()]));
			});
			if(finalUserGroupRedisClient != null){
				if(backup){
					finalUserGroupRedisClient.rename(finalUserGroupBackupKey, finalUserGroupKey);
				}
				redisClientCache.releaseRedisClient(finalUserGroupDataConfig.getRedisInfo(), finalUserGroupRedisClient);
			}
		}
		return result;
	}

	private int writeDataToRedis(UserGroupSerDeserializer serDeserializer, Set<String> dataSet){
		for(String data: dataSet){
			if(data != null){
				serDeserializer.add(data);
			}
		}
		serDeserializer.serialize();
		return serDeserializer.getRowCount();
	}

	private Set<String> doDataOperation(String operation, Set<String> data1, Set<String> data2){
		switch (operation){
			case AND_OPERATION :
				return DataIntersection(data1, data2);
			case OR_OPERATION :
				return DataUion(data1, data2);
			case EXCLUDE_OPERATION:
				return DataDifference(data1, data2);
			default :
				break;
		}
		throw new UnsupportedOperationException(String.format("Can not do operation[%s] for user group data", operation));
	}

	//并集
	private Set<String> DataUion(Set<String> data1, Set<String> data2){
		data1.addAll(data2);
		return data1;
	}

	//交集
	private Set<String> DataIntersection(Set<String> data1, Set<String> data2){
		data1.retainAll(data2);
		return data1;
	}

	//补集 'data1-data2'
	private Set<String> DataDifference(Set<String> data1, Set<String> data2){
		data1.removeAll(data2);
		return data1;
	}

	private Long deleteUserGroups(RedisInfo redisInfo, String... userGroupKeys){
		if(userGroupKeys == null || userGroupKeys.length == 0){
			log.warn(String.format("The userGroupKeys to delete is empty with %s", redisInfo));
			return 0L;
		}
		RedisClientWrapper redisClient = redisClientCache.getRedisClient(redisInfo);
		Long result = redisClient.del(userGroupKeys);
		log.info(String.format("Delete userGroups %s with config: %s", Arrays.toString(userGroupKeys), redisInfo));
		redisClientCache.releaseRedisClient(redisInfo, redisClient);
		return result;
	}

	private boolean backupRedisData(RedisClientWrapper redisClient, String redisKey, String backupKey){
		if(!redisClient.exists(redisKey)){
			log.info(String.format("Can not backup usergroup data on redis with key [%s], beacuse it doesn't exists.",redisKey));
			// no need to backup
			return false;
		}
		redisClient.rename(redisKey,backupKey);
		log.info(String.format("Backup usergroup data on redis with key [%s], backupKey [%s]",redisKey, backupKey));
		return true;
	}

	private String generateRedisBackUpKey(String originalKey){
		return new StringBuffer(originalKey).append("backup").toString();
	}
}
