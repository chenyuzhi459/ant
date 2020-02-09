package io.sugo.services.usergroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import io.sugo.common.redis.RedisInfo;
import io.sugo.common.utils.*;
import io.sugo.server.http.Configure;
import io.sugo.services.cache.Caches;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.common.redis.RedisClientWrapper;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.services.usergroup.bean.usergroup.*;
import io.sugo.services.usergroup.query.UserGroupQuery;
import io.sugo.common.guice.annotations.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import io.sugo.services.usergroup.OperationResult.OperationStatus;

import javax.inject.Named;

import static io.sugo.common.utils.Constants.SYSTEM_PROPS;
import static io.sugo.common.utils.Constants.Sys.*;
import static io.sugo.common.utils.Constants.UserGroup.CONSUMER_RUN_INTERVAL;
import static io.sugo.common.utils.Constants.UserGroup.CONSUMER_THREAD_SIZE;
import static io.sugo.server.http.resource.UserGroupResource.*;
import static io.sugo.common.utils.UserGroupUtil.*;

public class UserGroupHelper implements AntService {
	private static final Logger log = LogManager.getLogger(UserGroupHelper.class);

	@Inject
	private static Configure configure;
	@Inject @Named(Constants.UserGroup.QUERY_QUEUE_REDIS_KEY)
	public static  String QUEUE_REDIS_KEY;
	@Inject @Named(Constants.UserGroup.QUERY_RESULT_REDIS_KEY)
	public static  String RESULT_REDIS_KEY ;
	// Use to synchronize start() and stop(). These methods should be synchronized to prevent from being called at the
	// same time if two different threads are calling them. This might be possible if a druid coordinator gets and drops
	// leadership repeatedly in quick succession.
	private final Object lock = new Object();

	private final ObjectMapper jsonMapper;
	private final Caches.RedisClientCache redisClientCache;

	private volatile ListeningScheduledExecutorService consumerExec = null;
	private volatile ListenableFuture<?>  consumerFuture = null;
	private volatile boolean started;
	private final RedisInfo systemRedisInfo;


	@Inject
	public UserGroupHelper(@Json ObjectMapper jsonMapper,
						   Caches.RedisClientCache redisClientCache) {
		log.info("create new UserGroupHelper...");
		this.jsonMapper = jsonMapper;
		this.redisClientCache = redisClientCache;
		this.systemRedisInfo =  new RedisInfo(
				configure.getProperty(SYSTEM_PROPS, REDIS_HOST_AND_PORT),
				configure.getBoolean(SYSTEM_PROPS, REDIS_CLUSTER_MODE,false),
				configure.getBoolean(SYSTEM_PROPS, REDIS_SENTINEL_MODE,false),
				configure.getProperty(SYSTEM_PROPS, REDIS_MASTER_NAME,null),
				configure.getProperty(SYSTEM_PROPS, REDIS_PASSWORD,null));

	}

	public void start(){
		synchronized (lock) {
			if (started) {
				return;
			}
			consumerExec = MoreExecutors.listeningDecorator(ExecUtil.scheduledMutilThread(
					configure.getInt(SYSTEM_PROPS, CONSUMER_THREAD_SIZE, 5),
					"UserGroup-Consumer-Exec--%d"));

			consumerFuture = consumerExec.scheduleWithFixedDelay(() ->{
				try {
					consume();
				} catch (Exception e) {
					log.error("UserGroup consumer error.",e);
				}
			},
			0,
			configure.getInt(SYSTEM_PROPS, CONSUMER_RUN_INTERVAL, 30),
			TimeUnit.SECONDS);

			log.info("start consumer for usergroup...");
			started = true;

		}
	}

	public void stop() {
		synchronized (lock) {
			if (!started) {
				return;
			}

			consumerFuture.cancel(false);
			consumerExec.shutdown();
			consumerExec = null;
			log.info("stop  producer and consumer for usergroup...");
			started = false;
		}
	}

	private void consume() throws Exception{

		RedisClientWrapper systemRedisClient = null;
		try {
			//jedis实例非线程安全,所以每个线程从缓存中获取一个实例
			systemRedisClient = redisClientCache.getRedisClient(systemRedisInfo);
			String operationBodyStr = null;
			while(( operationBodyStr = systemRedisClient.rpop(QUEUE_REDIS_KEY)) != null){
				MutliOperationBody operation = this.jsonMapper.readValue(operationBodyStr, MutliOperationBody.class);
				if(operation == null){
					return;
				}

				String id = operation.getId();
				log.info(String.format("found operation[id=%s]", id ));

				OperationResult operationResult = getOperationResult(RESULT_REDIS_KEY, id);
				operationResult.setStartTime(System.currentTimeMillis()).setStatus(OperationStatus.RUNNING);
				this.updateOperationResult(RESULT_REDIS_KEY, operationResult);
				log.info(String.format("update usergroup mutliOperation[id=%s] status to %s", id, operationResult.getStatus()));
				this.doMultiUserGroupOperationV2(operation);
			}

		} finally {
			redisClientCache.releaseRedisClient(systemRedisInfo, systemRedisClient);
		}

	}


	public OperationResult addOperation(MutliOperationBody operation) throws JsonProcessingException {
		RedisClientWrapper systemRedisClient = null;
		try {
			systemRedisClient = redisClientCache.getRedisClient(systemRedisInfo);
			String operationStr = this.jsonMapper.writeValueAsString(operation);

			systemRedisClient.lpush(QUEUE_REDIS_KEY,operationStr);
			OperationResult operationResult = new OperationResult(operation.getId());
			operationResult.setRequestTime(System.currentTimeMillis()).setStatus(OperationStatus.ACCEPTED);
			this.updateOperationResult(RESULT_REDIS_KEY, operationResult);
			log.info("accept new usergroup mutliOperation, id : " + operation.getId());
			return operationResult;
		}finally {
			redisClientCache.releaseRedisClient(systemRedisInfo,systemRedisClient);
		}

	}

	public void updateOperationResult(String key, OperationResult operationResult)  {
		RedisClientWrapper systemRedisClient = null;
		try {
			systemRedisClient = redisClientCache.getRedisClient(systemRedisInfo);
			systemRedisClient.hset(key, operationResult.getId(), jsonMapper.writeValueAsString(operationResult));
		} catch (Exception e) {
			log.error(String.format("update operationResult[id=%s] failed", operationResult.getId()),e);
		}finally {
			redisClientCache.releaseRedisClient(systemRedisInfo, systemRedisClient);
		}
	}

	public OperationResult getOperationResult(String key, String id) throws IOException {
		RedisClientWrapper systemRedisClient = null;
		try {
			systemRedisClient = redisClientCache.getRedisClient(systemRedisInfo);
			String operationResultStr = systemRedisClient.hget(key,id);
			if(null == operationResultStr){
				return null;
			}
			return jsonMapper.readValue(operationResultStr, OperationResult.class);
		} finally {
			redisClientCache.releaseRedisClient(systemRedisInfo, systemRedisClient);
		}

	}

	private void removeOperationResults(String key, String... ids){
		RedisClientWrapper systemRedisClient = null;
		try {
			systemRedisClient = redisClientCache.getRedisClient(systemRedisInfo);
			systemRedisClient.hdel(key, ids);
			log.info(String.format("remove operationResults, ids=%s ", Arrays.toString(ids)));
		}finally {
			redisClientCache.releaseRedisClient(systemRedisInfo, systemRedisClient);
		}
	}

	public List<OperationResult> fetchOperationResultByIds(String... ids) throws IOException {
		List<OperationResult> results = new ArrayList<>();
		if(ids.length > 0){
			for(String id : ids){
				OperationResult operationResult = this.getOperationResult(RESULT_REDIS_KEY,id);
				if(operationResult == null){
					log.warn(String.format("operationResult for id=%s is null, ignore that.", id));
					continue;
				}
				results.add(operationResult);
				if(OperationStatus.isFinished(operationResult.getStatus())){
					this.removeOperationResults(RESULT_REDIS_KEY,id);
				}
			}
		} else {
			RedisClientWrapper systemRedisClient = null;
			try {
				systemRedisClient = redisClientCache.getRedisClient(systemRedisInfo);
				Map<String,String> allResutls = systemRedisClient.hgetAll(RESULT_REDIS_KEY);
				for(Map.Entry<String,String> entry: allResutls.entrySet()){
					String id = entry.getKey();
					String val = entry.getValue();
					OperationResult operationResult = jsonMapper.readValue(val,OperationResult.class);
					results.add(operationResult);

					if(OperationStatus.isFinished(operationResult.getStatus())){
						this.removeOperationResults(RESULT_REDIS_KEY,id);
					}
				}
			}finally {
				redisClientCache.releaseRedisClient(systemRedisInfo,systemRedisClient);
			}
		}


		return results;
	}

	public List<Map> doMultiUserGroupOperation(Map<String, List<GroupBean>> userGroupParams){
		List<Map> result;
		log.info("Begin to doMultiUserGroupOperation...");
		long startMillis = System.currentTimeMillis();
		FinalGroupBean finalGroup = (FinalGroupBean)userGroupParams.get("finalGroup").get(0);
		List<GroupBean> assistantGroupList = userGroupParams.get("AssistantGroupList");

		RedisDataIOFetcher finalUserGroupDataConfig = finalGroup.getQuery().getDataConfig();
		String finalUserGroupKey = finalUserGroupDataConfig.getGroupId();
		String finalUserGroupBackupKey = generateRedisBackUpKey(finalUserGroupKey);
		RedisClientWrapper finalUserGroupRedisClient = redisClientCache.getRedisClient(finalUserGroupDataConfig.getRedisInfo());
		Set<String> acculatedData = new HashSet<>();
		Set<String> currentData = new HashSet<>();
		boolean backup = false;
		try {
			if(assistantGroupList== null || assistantGroupList.isEmpty()){
				return Collections.singletonList(ImmutableMap.of("error", "AssistantGroup array is empty"));
			}

			for(int i = 0; i < assistantGroupList.size(); i++){
				UserGroupBean userGroupBean = (UserGroupBean)assistantGroupList.get(i);

				String op = userGroupBean.getOp();
				currentData = userGroupBean.getData();
				userGroupBean.close();
				if(i == 0){
					//第一个的数据集默认全部并入acculatedData
					op = OR_OPERATION;
				}
				if(!op.isEmpty()){
					acculatedData = UserGroupUtil.doDataOperation(op, acculatedData, currentData);
				}
				currentData.clear();
			}

			UserGroupSerDeserializer finalSerDeserializer = new UserGroupSerDeserializer(finalUserGroupDataConfig);

			if(finalGroup.isAppend()){
				// do 'append operation'
				currentData = finalGroup.getData();
				acculatedData = UserGroupUtil.doDataOperation(OR_OPERATION, acculatedData, currentData);
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
			if(finalUserGroupRedisClient != null){
				if(backup){
					finalUserGroupRedisClient.rename(finalUserGroupBackupKey, finalUserGroupKey);
				}
				redisClientCache.releaseRedisClient(finalUserGroupDataConfig.getRedisInfo(), finalUserGroupRedisClient);
			}
		}
		return result;
	}

	public void doMultiUserGroupOperationV2(MutliOperationBody operation) throws Exception  {
		Map<String, List<GroupBean>> userGroupParams = UserGroupUtil.parseMultiUserGroupParam(operation.getGroups());
		String operationId = operation.getId();
		log.info("Begin to doMultiUserGroupOperationV2...");
		long startMillis = System.currentTimeMillis();
		FinalGroupBean finalGroup = (FinalGroupBean)userGroupParams.get("finalGroup").get(0);
		List<GroupBean> assistantGroupList = userGroupParams.get("AssistantGroupList");

		RedisDataIOFetcher finalUserGroupDataConfig = finalGroup.getQuery().getDataConfig();
		RedisClientWrapper finalUserGroupRedisClient = redisClientCache.getRedisClient(finalUserGroupDataConfig.getRedisInfo());
		Set<String> acculatedData = new HashSet<>();
		Set<String> currentData = new HashSet<>();
		try {
			if(assistantGroupList== null || assistantGroupList.isEmpty()){
				return;
			}

			for(int i = 0; i < assistantGroupList.size(); i++){
				UserGroupBean userGroupBean = (UserGroupBean)assistantGroupList.get(i);
				String op = userGroupBean.getOp();
				currentData = userGroupBean.getData();

				if(i == 0){
					op = OR_OPERATION;
				}
				if(!op.isEmpty()){
					acculatedData = UserGroupUtil.doDataOperation(op, acculatedData, currentData);
				}
				userGroupBean.updateParsedData(currentData, operationId, this);
				userGroupBean.close();
				currentData.clear();
			}

			if(finalGroup.isAppend()){
				// do 'append operation'
				currentData = finalGroup.getData();
				acculatedData = UserGroupUtil.doDataOperation(OR_OPERATION, acculatedData, currentData);
				currentData.clear();
			}
			UserGroupSerDeserializer finalSerDeserializer = new UserGroupSerDeserializer(finalUserGroupDataConfig);
			int finalLen = writeDataToRedis(finalSerDeserializer, acculatedData);
			acculatedData.clear();

			OperationResult operationResult = this.getOperationResult(RESULT_REDIS_KEY, operationId);
			operationResult.setEndTime(System.currentTimeMillis())
					.setUseGroupSize(finalLen)
					.setStatus(OperationStatus.SUCCESS);
			this.updateOperationResult(RESULT_REDIS_KEY, operationResult);
			long endMillis = System.currentTimeMillis();
			log.info(String.format("MultiUserGroupOperationV2 success for [id=%s] total cost %d sec.", operationId, (endMillis - startMillis)/1000));
		}catch (Exception e){
			log.error(String.format("MultiUserGroupOperationV2 failed for operation[id=%s] failed",operation.getId()), e);
			OperationResult operationResult = this.getOperationResult(RESULT_REDIS_KEY, operationId);
			operationResult.setEndTime(System.currentTimeMillis()).setStatus(OperationStatus.FAILED);
			this.updateOperationResult(RESULT_REDIS_KEY, operationResult);
		} finally {
			acculatedData.clear();
			currentData.clear();
			if(finalUserGroupRedisClient != null){
				redisClientCache.releaseRedisClient(finalUserGroupDataConfig.getRedisInfo(), finalUserGroupRedisClient);
			}

		}

	}

	/**
	 *检查分群之间是否两两互斥
	 * 从第一个分群开始逐个与后面直到最后一个比较(比如一次检查5个分群,则总共检查4+3+2+1=10c次)
	 */

	public List<Map> checkMutex(Map<String, List<GroupBean>> userGroupParams){
		log.info("Begin to checkMutex...");
		long startMillis = System.currentTimeMillis();
		List<Map> result;
		Map<String, Set<String>> intersectGroups = new HashMap<>();
		Map<String, Set<String>> dataMap = new HashMap<>();
		List<GroupBean> assistantGroupList = userGroupParams.get("AssistantGroupList");

		Set<String> firstGroupData;
		Set<String> secondGroupData;
		try {
			if(assistantGroupList== null || assistantGroupList.isEmpty()){
				return Collections.singletonList(ImmutableMap.of("error", "AssistantGroup array is empty"));
			}

			for(int i = 0; i < assistantGroupList.size(); i++){
				UserGroupBean userGroupBean = (UserGroupBean)assistantGroupList.get(i);

				boolean isTempUserGroup = UserGroupBean.INDEX_TYPES.contains(userGroupBean.getType());
				if(isTempUserGroup){
					String broker;
					if(userGroupBean instanceof UindexGroupBean){
						broker = ((UindexGroupBean) userGroupBean).getBroker();
					}else {
						broker = ((TindexGroupBean) userGroupBean).getBroker();
					}
					UserGroupQuery query = (UserGroupQuery)userGroupBean.getQuery();
					QueryUtil.getUserGroupQueryResult(broker, query);
				}
			}

			for(int i = 0; i < assistantGroupList.size(); i++){
				UserGroupBean userGroup1 = (UserGroupBean)assistantGroupList.get(i);
				RedisDataIOFetcher redisDataFetcher1 = userGroup1.getQuery().getDataConfig();
				firstGroupData = getDataWithCache(dataMap, redisDataFetcher1);

				//从当前位置的后面开始移动
				for(int j = i + 1;  j< assistantGroupList.size(); j++){
					UserGroupBean userGroup2 = (UserGroupBean)assistantGroupList.get(j);
					RedisDataIOFetcher redisDataFetcher2 = userGroup2.getQuery().getDataConfig();
					secondGroupData = getDataWithCache(dataMap, redisDataFetcher2);
					//与doMultiUserGroupOperation的And Operation不同, 这里要把分群数据缓存起来,用于后续的比较,避免每次都去redis取
					Set<String> intersectData = DataIntersectionWithoutChange(firstGroupData, secondGroupData);
					if(intersectData == null || intersectData.isEmpty()) continue;
					intersectGroups.computeIfAbsent(redisDataFetcher1.getGroupId(), (k)-> new HashSet<>())
							.add(redisDataFetcher2.getGroupId());
				}
			}

			result = Collections.singletonList(ImmutableMap.of(
					"status", "success",
					"size", intersectGroups.size(),
					"result", intersectGroups
					));
			long endMillis = System.currentTimeMillis();
			log.info(String.format("CheckMutex total cost %d ms.", endMillis - startMillis));
		}finally {
			dataMap.clear();
		}
		return result;
	}

	private Set<String> getDataWithCache(Map<String, Set<String>> cache, RedisDataIOFetcher fetcher){
		UserGroupSerDeserializer itemSerDeserializer = new UserGroupSerDeserializer(fetcher);
		return cache.computeIfAbsent(fetcher.getGroupId(),(k) ->{
			Set<String> data = new HashSet<>();
			itemSerDeserializer.deserialize(data);
			return data;
		});
	}

	private int writeDataToRedis(UserGroupSerDeserializer serDeserializer, Set<String> dataSet){
		return UserGroupUtil.writeDataToRedis(serDeserializer, dataSet);
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
