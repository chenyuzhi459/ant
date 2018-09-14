package io.sugo.server.usergroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.common.utils.JsonObjectIterator;
import io.sugo.server.http.resource.usergroup.UserGroupResource;
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

import java.io.IOException;
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

	public enum GroupOperation {
		AND("and"),
		OR("or"),
		EXECLUDE("exclude");
		private final String value;
		GroupOperation(String value){
			this.value = value;
		}
		public String getValue(){
			return this.value;
		}
	}

	public static void main(String[] args) throws IOException {
//		testDoUserGroupQueryIncremental();
//		testMultiUserGroupOperation();
		log.error( "test",new Throwable("ssss"));
	}

	public static void testMultiUserGroupOperation() throws IOException {
		String paramStr = "[\n" +
				"  {\n" +
				"      \"type\": \"tindex\",\n" +
				"      \"brokerUrl\": \"http://192.168.0.225:8082/druid/v2?pretty\",\n" +
				"      \"query\": {\n" +
				"            \"queryType\":\"user_group\",\n" +
				"            \"dataSource\":\"schedule_desc\",\n" +
				"            \"granularity\":\"all\",\n" +
				"            \"intervals\": \"1000/3000\",\n" +
				"            \"filter\": {\n" +
				"                \"type\": \"selector\",\n" +
				"                \"dimension\": \"sugo_province\",\n" +
				"                \"value\": \"广东省\"\n" +
				"            },\n" +
				"            \"dimension\":\"distinct_id\",\n" +
				"            \"dataConfig\": {\n" +
				"                \"hostAndPorts\":\"192.168.0.223:6379\",  \n" +
				"                \"clusterMode\":false,  \n" +
				"                \"groupId\":\"schedule_desc_sugo_province\"  \n" +
				"            },\n" +
				"            \"context\":{\n" +
				"                \"timeout\": 180000,\n" +
				"                \"useOffheap\": true,\n" +
				"                \"groupByStrategy\": \"v2\"\n" +
				"            }\n" +
				"      }\n" +
				"  },\n" +
				"\n" +
				"  {\n" +
				"    \"type\": \"usergroup\",\n" +
				"    \"query\": {\n" +
				"      \"dataConfig\": { \n" +
				"            \"hostAndPorts\": \"192.168.0.220:6379\",\n" +
				"            \"clusterMode\": false,\n" +
				"            \"sentinelMode\": false,\n" +
				"            \"groupId\": \"usergroup_HJMqcLLtG\"\n" +
				"      }\n" +
				"    },\n" +
				"    \"op\": \"or\"\n" +
				"  },\n" +
				"  {\n" +
				"      \"type\": \"uindex\",\n" +
				"      \"brokerUrl\": \"http://192.168.0.223:8082/druid/v2?pretty\",\n" +
				"      \"query\": {\n" +
				"                \"queryType\":\"user_group\",\n" +
				"                \"dataSource\":\"tag_bank\",\n" +
				"                \"granularity\":\"all\",\n" +
				"                \"intervals\": \"1000/3000\",\n" +
				"                \"filter\": {\n" +
				"                    \"type\": \"selector\",\n" +
				"                    \"dimension\": \"ub_risk\",\n" +
				"                    \"value\": \"R4\"\n" +
				"                },\n" +
				"                \"dimension\":\"distinct_id\",\n" +
				"                \"dataConfig\": {\n" +
				"                    \"hostAndPorts\":\"192.168.0.223:6379\",  \n" +
				"                    \"clusterMode\":false,  \n" +
				"                    \"groupId\":\"tag_bank_ub_risk\"  \n" +
				"                },\n" +
				"                \"context\":{\n" +
				"                    \"timeout\": 180000,\n" +
				"                    \"useOffheap\": true,\n" +
				"                    \"groupByStrategy\": \"v2\"\n" +
				"                }\n" +
				"      },\n" +
				"      \"op\": \"exclude\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"type\": \"finalGroup\",\n" +
				"    \"query\": {\n" +
				"      \"dataConfig\": { \n" +
				"            \"hostAndPorts\":\"192.168.0.223:6379\",  \n" +
				"            \"clusterMode\":false,  \n" +
				"            \"groupId\":\"test_usergroup_multi\"  \n" +
				"      }\n" +
				"    }\n" +
				"  }\n" +
				"]";
		ObjectMapper objectMapper = new ObjectMapper();
		UserGroupHelper helper = new UserGroupHelper(objectMapper, RedisClientCache.getInstance());
		UserGroupResource resource = new UserGroupResource(objectMapper, helper);
		List<Map<String, Object>> params = (List<Map<String, Object>> )objectMapper.readValue(paramStr, List.class);
		Map<String, Object> paramsMap = resource.parseMultiUserGroupParam(params);
		helper.doMultiUserGroupOperation(paramsMap);
		Map<String, Object> finalGroup = (Map<String, Object> )paramsMap.get("finalGroup");
		UserGroupQuery finalQuery = (UserGroupQuery) finalGroup.get("query");
		log.info(testReadDataFromRedis(finalQuery.getDataConfig()).toString());
	}

	public static void testDoUserGroupQueryIncremental() throws IOException {
		String queryStr = "{\n" +
				"    \"queryType\":\"user_group\",\n" +
				"    \"dataSource\":\"userinfo\",\n" +
				"    \"granularity\":\"all\",\n" +
				"    \"intervals\": \"1000/3000\",\n" +
				"    \"filter\": {\n" +
				"        \"type\": \"selector\",\n" +
				"        \"dimension\": \"province\",\n" +
				"        \"value\": \"广东省\"\n" +
				"    },\n" +
				"    \"dimension\":\"province\",\n" +
				"    \"dataConfig\": {\n" +
				"        \"hostAndPorts\":\"192.168.0.223:6379\",  \n" +
				"        \"clusterMode\":false,  \n" +
				"        \"groupId\":\"usergrpu_1\"  \n" +
				"    },\n" +
				"    \"aggregations\":[\n" +
				"        {\n" +
				"            \"name\": \"sum(age)\",\n" +
				"            \"type\": \"lucene_doubleSum\",\n" +
				"            \"fieldName\": \"age\"\n" +
				"        }\n" +
				"    ],\n" +
				"    \"context\":{\n" +
				"        \"timeout\": 180000,\n" +
				"        \"useOffheap\": true,\n" +
				"        \"groupByStrategy\": \"v2\"\n" +
				"    }\n" +
				"}";
		ObjectMapper objectMapper = new ObjectMapper();
		UserGroupQuery userGroupQuery = objectMapper.readValue(queryStr, UserGroupQuery.class);
		UserGroupHelper helper = new UserGroupHelper(objectMapper, RedisClientCache.getInstance());
		List<Map> result = helper.doUserGroupQueryIncremental(userGroupQuery, "http://192.168.0.212:8082/druid/v2?pretty");
		log.info(testReadDataFromRedis(userGroupQuery.getDataConfig()).toString());
	}

	public static Set<String> testReadDataFromRedis(RedisDataIOFetcher redisIOFactory ){
		UserGroupSerDeserializer serDeserializer = new UserGroupSerDeserializer(redisIOFactory);
		Set<String> data = new HashSet<>();
		serDeserializer.deserialize(data);
		return data;
	}

	public List<Map> getUserGroupQueryResult(UserGroupQuery query, String brokerUrl){
		List<Map> result = new ArrayList<>();

		try {
			String queryStr = jsonMapper.writeValueAsString(query);
			log.info("Begin to request getUserGroupQueryResult...");
			log.info(String.format("RequestMetada = Broker url: %s . Param: %s", brokerUrl, queryStr));
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
					throw new UserGroupException(response.body().string());
				}

				long after = System.currentTimeMillis();
				log.info(String.format("GetUserGroupQueryResult total cost %d million seconds.", after - before));
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

				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("event", ImmutableMap.of("RowCount", finalLen));
				result.clear();
				result.add(resultMap);
			}
		}catch (Throwable t){

			log.error( "Do userGroupQueryIncremental occurs error!",t);
			result = Collections.singletonList(ImmutableMap.of("error", t.getMessage()));
		}finally {
			currentData.clear();
			backupData.clear();
			currentData = null;
			backupData = null;

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
				itemSerDeserializer.deserialize(i == 0 ? acculatedData :currentData);
				if(i == 0){
					continue;
				}
				if(!op.isEmpty()){
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
		}catch (Throwable t){
			log.error("Do multiUserGroupOperation occurs error!",t);
			result = Collections.singletonList(ImmutableMap.of("error", t.getMessage()));
		}finally {
			acculatedData.clear();
			currentData.clear();
			acculatedData = null;
			currentData = null;
			tempUserGroupMap.forEach((redisInfo, userGroupKey) ->{
				deleteUserGroups(redisInfo, userGroupKey.toArray(new String[userGroupKey.size()]));
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
			log.warn(String.format("Can not backup usergroup data on redis with key [%s], beacuse it doesn't exists.",redisKey));
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

	private RedisClientWrapper getRedisClient(RedisInfo redisInfo){
		return new RedisClientWrapper(redisInfo);
	}
}