package io.sugo.server.usergroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.sugo.common.utils.JsonObjectIterator;
import io.sugo.server.redis.RedisClientCache;
import io.sugo.server.redis.RedisDataIOFetcher;
import io.sugo.server.redis.RedisClientWrapper;
import io.sugo.server.redis.RedisInfo;
import io.sugo.server.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.server.usergroup.exception.UserGroupException;
import io.sugo.server.usergroup.model.UserGroupQuery;
import io.sugo.common.guice.annotations.Json;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.InputStream;
import java.util.*;

public class UserGroupHelper {
	private static final Logger log = LogManager.getLogger(UserGroupHelper.class);
	private final ObjectMapper jsonMapper;
	private final RedisClientCache redisClientCache;
	private static final String AND_OPERATION = "and";
	private static final String OR_OPERATION = "or";
	private static final String EXCLUDE_OPERATION = "exclude";

	@Inject
	public UserGroupHelper(@Json ObjectMapper jsonMapper, RedisClientCache redisClientCache) {
		this.jsonMapper = jsonMapper;
		this.redisClientCache = redisClientCache;
	}

	public List<Map> getUserGroupQueryResult(UserGroupQuery query, String brokerUrl){
		List<Map> result = new ArrayList<>();

		try {
			String queryStr = jsonMapper.writeValueAsString(query);
			log.info(String.format("Begin to request getUserGroupQueryResult, requestMetada:\n" +
					">>>>>>>>>>>>>>>>\n " +
					"UserGroup query url= %s . Param= %s\n" +
					"<<<<<<<<<<<<<<<<", brokerUrl, queryStr));

			long before = System.currentTimeMillis();
			OkHttpClient client = new OkHttpClient();
			RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), queryStr);
			Request request = new Request.Builder().url(brokerUrl).post(body).build();

			Response response = client.newCall(request).execute();
			try {
				if(response.code() == 200){
					InputStream stream = response.body().byteStream();
					JsonObjectIterator iterator = new JsonObjectIterator(stream);

					while (iterator.hasNext()) {
						HashMap resultValue = iterator.next();
						if (resultValue != null) {
							result.add(resultValue);
						}
					}
				}else {
					String errStr = response.body().string();
					Object originalMessage = null;
					try {
						originalMessage = jsonMapper.readValue(errStr, Object.class);
					}catch (Exception e){
					}
					throw new UserGroupException(originalMessage, errStr);
				}

				long after = System.currentTimeMillis();
				log.info(String.format("GetUserGroupQueryResult total cost %d ms.", after - before));
			}finally {
				if(response != null){
					//close response to avoid memery leak
					response.close();
				}
			}

		} catch (Throwable t) {
			log.error("Get userGroupQueryResult error.", t);
			Throwables.propagate(t);
		}

		return result;
	}

	public List<Map> doUserGroupQueryIncremental(UserGroupQuery query, String brokerUrl){
		List<Map> result = new ArrayList<>();
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
			Map queryMap = result.get(0);
			//prune queryMap
			queryMap.remove("v");
			queryMap.computeIfPresent("event", (key, value)->{
				Map<String, Object> eventMap = (Map)value;
				return Maps.filterKeys(eventMap, (key2)->{return key2.equals("RowCount");});
			});
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

				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("event", ImmutableMap.of("RowCount", finalLen));
				result.clear();
				result.add(resultMap);
				long endMillis = System.currentTimeMillis();
				log.info(String.format("UserGroupQueryIncremental total cost %d ms.", endMillis - startMillis));
			}
		} catch (UserGroupException ugException) {
			log.error( "Do userGroupQueryIncremental occurs remote exception!", ugException);
			result = Collections.singletonList(ImmutableMap.of("error",
					ugException.getOriginalMessage() != null ? ugException.getOriginalMessage() : ugException.getMessage()));
		}catch (Throwable t){
			log.error( "Do userGroupQueryIncremental occurs error!",t);
			result = Collections.singletonList(ImmutableMap.of("error", t.getMessage()));
		}finally {
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

	public List<Map> doMultiUserGroupOperation(Map<String, Object> userGroupParams){
		List<Map> result = new ArrayList<>();
		log.info("Begin to doMultiUserGroupOperation...");
		long startMillis = System.currentTimeMillis();
		Map<String, Object> finalGroup = (Map<String, Object> )userGroupParams.get("finalGroup");
		List<Map<String, Object>> userGroupList = (List<Map<String, Object>>)userGroupParams.get("userGroupList");
		Set<String> acculatedData = new HashSet<>();
		Set<String> currentData = new HashSet<>();

		UserGroupQuery finalUserGroupQuery = (UserGroupQuery)finalGroup.get("query");
		RedisDataIOFetcher finalUserGroupDataConfig = finalUserGroupQuery.getDataConfig();
		String finalUserGroupKey = finalUserGroupDataConfig.getGroupId();
		String finalUserGroupBackupKey = generateRedisBackUpKey(finalUserGroupKey);
		RedisClientWrapper finalUserGroupRedisClient = redisClientCache.getRedisClient(finalUserGroupDataConfig.getRedisInfo());
		Map<RedisInfo, Set<String>> tempUserGroupMap = new HashMap<>();
		boolean backup = false;
		try {
			if(userGroupList.isEmpty()){
				return Collections.singletonList(ImmutableMap.of("error", "userGroup array is empty"));
			}

			UserGroupSerDeserializer itemSerDeserializer;

			for(int i = 0; i < userGroupList.size(); i++){
				Map<String, Object> userGroupMap = userGroupList.get(i);
				String type = (String)userGroupMap.get("type");
				boolean isTempUserGroup = type.equals("tindex") || type.equals("uindex");
				String op = (String)userGroupMap.getOrDefault("op","");
				UserGroupQuery query = (UserGroupQuery)userGroupMap.get("query");
				if(isTempUserGroup){
					String brokerUrl = (String) userGroupMap.get("brokerUrl");
					getUserGroupQueryResult(query, brokerUrl);
					Set<String> userGroupKeys = tempUserGroupMap.computeIfAbsent(query.getDataConfig().getRedisInfo(), k -> new HashSet<>());
					userGroupKeys.add(query.getDataConfig().getGroupId());
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
			boolean isAppend = (Boolean) finalGroup.getOrDefault("append", false);
			if(isAppend){
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

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("event", ImmutableMap.of("RowCount", finalLen));
			result.add(resultMap);
			long endMillis = System.currentTimeMillis();
			log.info(String.format("MultiUserGroupOperation total cost %d ms.", endMillis - startMillis));
		}catch (UserGroupException ugException) {
			log.error( "Do multiUserGroupOperation occurs remote exception!", ugException);
			result = Collections.singletonList(ImmutableMap.of("error",
					ugException.getOriginalMessage() != null ? ugException.getOriginalMessage() : ugException.getMessage()));
		}catch (Throwable t){
			log.error("Do multiUserGroupOperation occurs error!",t);
			result = Collections.singletonList(ImmutableMap.of("error", t.getMessage()));
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
