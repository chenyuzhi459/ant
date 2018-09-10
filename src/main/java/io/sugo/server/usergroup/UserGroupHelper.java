package io.sugo.server.usergroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.sugo.common.utils.JsonObjectIterator;
import io.sugo.server.http.resource.usergroup.UserGroupResource;
import io.sugo.server.redis.DataRedisIOFactory;
import io.sugo.server.redis.RedisClientWrapper;
import io.sugo.server.redis.RedisInfo;
import io.sugo.server.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.server.usergroup.exception.UserGroupException;
import io.sugo.server.usergroup.model.UserGroupQuery;
import io.sugo.server.guice.annotations.Json;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class UserGroupHelper {
//	private static final Logger log = new Logger(UserGroupHelper.class);
	private static final Logger log = LogManager.getLogger(UserGroupHelper.class);
	private final ObjectMapper jsonMapper;
	private static final String AND_OPERATION = "and";
	private static final String OR_OPERATION = "or";
	private static final String EXCLUDE_OPERATION = "exclude";

	@Inject
	public UserGroupHelper(@Json ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
//		this.jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
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
		UserGroupHelper helper = new UserGroupHelper(objectMapper);
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
		UserGroupHelper helper = new UserGroupHelper(objectMapper);
		List<Map> result = helper.doUserGroupQueryIncremental(userGroupQuery, "http://192.168.0.212:8082/druid/v2?pretty");
		log.info(testReadDataFromRedis(userGroupQuery.getDataConfig()).toString());
	}

	public static Set<String> testReadDataFromRedis(DataRedisIOFactory redisIOFactory ){
		UserGroupSerDeserializer serDeserializer = new UserGroupSerDeserializer(redisIOFactory);
		Set<String> data = new HashSet<>();
		serDeserializer.deserialize(data);
		return data;
	}

	public List<Map> getUserGroupQueryResult(UserGroupQuery query, String brokerUrl){
		List<Map> result = new ArrayList<>();

		try {
			String queryStr = jsonMapper.writeValueAsString(query);
			log.info("Begin to UserGroup...");
			log.info(String.format("Broker url: %s . Param: %s", brokerUrl, queryStr));
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
					response.close();
				}else {
					throw new UserGroupException(response.body().string());
				}

				long after = System.currentTimeMillis();
				log.info(String.format("getUserGroupQueryResult total cost %d million seconds.", after - before));
			}finally {
				if(response != null){
					//close response to avoid memery leak
					response.close();
				}
			}

		} catch (Throwable t) {
			log.error("getUserGroupQueryResult error.", t);
			Throwables.propagate(t);
		}

		return result;
	}

	public List<Map> doUserGroupQueryIncremental(UserGroupQuery query, String brokerUrl){
		List<Map> result = new ArrayList<>();
		DataRedisIOFactory redisIOFactory = query.getDataConfig();
		RedisClientWrapper redisClient = getRedisClient(redisIOFactory.getRedisInfo());
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
				doDataOperation(AND_OPERATION, currentData, backupData);
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

			log.error( "doUserGroupQueryIncremental occurs error!",t);
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
				redisClient.close();
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
		DataRedisIOFactory finalUserGroupDataConfig = finalUserGroupQuery.getDataConfig();
		String finalUserGroupKey = finalUserGroupDataConfig.getGroupId();
		RedisClientWrapper finalUserGroupRedisClient = getRedisClient(finalUserGroupDataConfig.getRedisInfo());
		String finalUserGroupBackupKey = generateRedisBackUpKey(finalUserGroupKey);
		boolean backup = false;
		try {
			if(userGroupList.isEmpty()){
				return Collections.singletonList(ImmutableMap.of("error", "userGroup array is empty"));
			}
			if(userGroupList.size() == 1){
				Map<String, Object> userGroupMap = userGroupList.get(0);
				String type = (String)userGroupMap.get("type");
				if(type.equals("tindex")|| type.equals("uindex")){
					String brokerUrl = (String) userGroupMap.get("brokerUrl");
					UserGroupQuery query = (UserGroupQuery)userGroupMap.get("query");
					result = getUserGroupQueryResult(query, brokerUrl);
				}else {
					result = Collections.singletonList(ImmutableMap.of("message", "the userGroup has in redis. "));
				}
				return result;
			}

			UserGroupSerDeserializer itemSerDeserializer;
			for(int i = 0; i < userGroupList.size(); i++){
				Map<String, Object> userGroupMap = userGroupList.get(i);
				String type = (String)userGroupMap.get("type");
				String op = (String)userGroupMap.getOrDefault("op","");
				UserGroupQuery query = (UserGroupQuery)userGroupMap.get("query");
				if(type.equals("tindex")|| type.equals("uindex")){
					String brokerUrl = (String) userGroupMap.get("brokerUrl");
					getUserGroupQueryResult(query, brokerUrl);
				}

				itemSerDeserializer = new UserGroupSerDeserializer(query.getDataConfig());
				if(i == 0){
					itemSerDeserializer.deserialize(acculatedData);
					continue;
				}
				itemSerDeserializer.deserialize(currentData);
				if(!op.isEmpty()){
					acculatedData = doDataOperation(op, acculatedData, currentData);
				}
				currentData.clear();
			}


			boolean isAppend = (Boolean) finalGroup.getOrDefault("append", false);

			UserGroupSerDeserializer finalSerDeserializer = new UserGroupSerDeserializer(finalUserGroupDataConfig);

			if(isAppend){
				finalSerDeserializer.deserialize(currentData);
				acculatedData = doDataOperation(OR_OPERATION, acculatedData, currentData);
				currentData.clear();
			}
			// do backup orperation
			backup = backupRedisData(finalUserGroupRedisClient, finalUserGroupKey, finalUserGroupBackupKey);
			int finalLen = writeDataToRedis(finalSerDeserializer, acculatedData);
			backup = false;
			finalUserGroupRedisClient.del(finalUserGroupBackupKey);
			acculatedData.clear();

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("event", ImmutableMap.of("RowCount", finalLen));
			result.add(resultMap);
		}catch (Throwable t){
			log.error("doMultiUserGroupOperation occurs error!",t);
			result = Collections.singletonList(ImmutableMap.of("error", t.getMessage()));
		}finally {
			acculatedData.clear();
			currentData.clear();
			acculatedData = null;
			currentData = null;

			if(finalUserGroupRedisClient != null){
				if(backup){
					finalUserGroupRedisClient.rename(finalUserGroupBackupKey, finalUserGroupKey);
				}
				finalUserGroupRedisClient.close();
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
		throw new UnsupportedOperationException(String.format("can not do operation[%s] for user group data", operation));
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
