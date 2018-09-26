package io.sugo.services.hive;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import io.sugo.common.utils.AntService;
import io.sugo.common.utils.CapacityMap;
import io.sugo.common.utils.StringUtil;
import io.sugo.services.hive.model.SQLBean;
import io.sugo.services.hive.client.HiveClient;
import io.sugo.server.http.Configure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.parquet.Strings;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;

import static io.sugo.services.cache.Caches.*;
import static io.sugo.common.utils.Constants.*;

public class SQLManager implements AntService{
	private static final Logger log = LogManager.getLogger(SQLManager.class);
	public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss.SSS");
	public static final BlockingQueue<SQLBean> PENDING_QUEUE = new LinkedBlockingQueue<>();
	public static Map<String,SQLResult> RESULT_MAP ;
	private static final int DEFAULT_EXECUTION_DURATION_SEC = 30;
	private static int DEFAULT_SQL_RESULT_LIVE_SIZE = 10;
	private volatile boolean started = false;
	private Configure configure;
	ScheduledExecutorService executorService;
	private HiveClientCache hiveClientCache;

	@Inject
	public SQLManager(Configure configure, HiveClientCache hiveClientCache) {
		this.configure = configure;
		this.hiveClientCache = hiveClientCache;
	}

	@Override
	public void start(){
		if(started) {
			return;
		}

		final int executeDurationSec =configure.getInt(HIVE_PROPS, Hive.HIVE_SQL_EXECUTE_DURATION_SEC, DEFAULT_EXECUTION_DURATION_SEC);
		final int resultLiveSize = configure.getInt(HIVE_PROPS, Hive.HIVE_SQL_RESULT_LIVE_SIZE, DEFAULT_SQL_RESULT_LIVE_SIZE);
		RESULT_MAP = new CapacityMap<>(resultLiveSize);

		executorService = Executors.newScheduledThreadPool(1,
				new ThreadFactoryBuilder().setNameFormat("SQLManager-Thread-%s").build());

		//定时执行sql计算任务
		executorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				while (PENDING_QUEUE.size()>0){
					log.info("Hive PENDING_QUEUE size:" + PENDING_QUEUE.size());
					SQLBean sqlBean = null;
					String queryId = null;
					try {
						sqlBean= PENDING_QUEUE.take();
						queryId = sqlBean.getQueryId();
						log.info(String.format("Get sql[%s] from queue to run.",queryId));
						List result = executeSQL(sqlBean);

						SQLResult sqlResult = new SQLResult(queryId,result,
								SQLResult.SUCCESS_STATUS,
								SQLResult.SUCCESS_MSG);
						RESULT_MAP.put(queryId,sqlResult);

					} catch (Exception e) {
						if(sqlBean == null || StringUtil.isEmpty(queryId)){
							return;
						}

						if(e.getMessage().contains("org.apache.thrift.transport.TTransportException")){
							retryGetNewConn(Hive.ASYNC_COMPUTE_CLIENT_KEY, 0);
							PENDING_QUEUE.offer(sqlBean);
							continue;
						}
						log.error(e.getMessage(),e);

						SQLResult sqlResult = new SQLResult(queryId, Collections.emptyList(),
								SQLResult.FAILED_STATUS,
								e.getMessage());
						RESULT_MAP.put(queryId,sqlResult);
					}
//					log.info("RESULT_MAP:"+ RESULT_MAP);
				}
			}
		},
		0,
		executeDurationSec,
		TimeUnit.SECONDS);

		started = true;
		log.info("Started SQLManager service.");
	}

	@Override
	public void stop(){
		if(started){
			executorService.shutdown();
		}
		started = false;
		log.info("Stopped SQLManager.");
	}


	public boolean retryGetNewConn(String key, long timeoutMillis){
		boolean result;
		HiveClient hiveClient = null;
		try {
			hiveClient = hiveClientCache.getHiveClient(key);
			result = hiveClient.retryToGetNewConn(timeoutMillis);
		}catch (Exception e){
			log.error("Retry get new  hiveConn error.", e);
			return false;
		}finally {
			hiveClientCache.releaseHiveClient(key, hiveClient);
		}

		return result;
	}

	public List executeSQL(SQLBean sqlBean) throws Exception{
		String queryId =  Strings.isNullOrEmpty(sqlBean.getQueryId()) ? "" : sqlBean.getQueryId();
		HiveClient hiveClient = null;
		String clientKey = Strings.isNullOrEmpty(queryId) ? Hive.SYNC_CLIENT_KEY : Hive.ASYNC_COMPUTE_CLIENT_KEY;
		try {
			log.info(String.format("Begin to execute sql[%s]...", queryId));
			long before = System.currentTimeMillis();
			hiveClient =hiveClientCache.getHiveClient(clientKey);
			List result = hiveClient.executeQuery(sqlBean);
			long after = System.currentTimeMillis();
			log.info(String.format("Sql[%s] has been executed successfully, total cost %d ms.", queryId, after-before));
			return result;
		}catch (Exception e){
			if(hiveClient != null){
				hiveClient.close();
			}
			throw Throwables.propagate(e);
		}finally {
			hiveClientCache.releaseHiveClient(clientKey, hiveClient);
		}
	}

	public Map cancel(List<String> queryIds){

		Map<String,List> resultMap = Maps.newHashMap();
		String successCancelKey = "success";
		String failedCancelKey = "failed";

		PENDING_QUEUE.removeIf(new java.util.function.Predicate<SQLBean>() {
			@Override
			public boolean test(SQLBean sqlBean) {
				String queryId = sqlBean.getQueryId();
				if(queryIds.contains(queryId)){
					queryIds.remove(queryId);
					List successList = resultMap.computeIfAbsent(successCancelKey, k -> Lists.newArrayList());
					successList.add(queryId);
					return true;
				}
				return false;
			}
		});

		for(String queryId:queryIds){
			try {
				HiveClient.cancel(queryId);
				List successList = resultMap.computeIfAbsent(successCancelKey, k -> Lists.newArrayList());
				successList.add(queryId);
			} catch (Exception e) {
				log.warn(String.format("Sql[%s] cancel failed, failed msg:%s",queryId,e.getMessage()));
				List failedList = resultMap.computeIfAbsent(failedCancelKey, k -> Lists.newArrayList());
				failedList.add(ImmutableMap.of("queryId",queryId,"message",e.getMessage()));
			}

		}
		return resultMap;
	}

	public static void addSqlBeanToPendingQueue(SQLBean sqlBean){
		PENDING_QUEUE.offer(sqlBean);
		log.info(String.format("Add sql[%s] to queue successfully",sqlBean.getQueryId()));
	}

	public static Collection<SQLBean> getSqlBeanInPendingQueue(List queryIds){
		return Collections2.filter(PENDING_QUEUE, new Predicate<SQLBean>() {
			@Override
			public boolean apply(@Nullable SQLBean sqlBean) {
				assert sqlBean != null;
				return queryIds.contains(sqlBean.getQueryId());
			}
		});
	}


	public class SQLResult {

		public static final String SUCCESS_STATUS = "success";
		public static final String SUCCESS_MSG = "ok";
		public static final String FAILED_STATUS = "failed";
		@JsonProperty
		String queryId;
		@JsonProperty
		List result;
		@JsonProperty
		String status;
		@JsonProperty
		String message;
		@JsonProperty
		String generateTime = TIME_FORMATTER.print(System.currentTimeMillis());
		boolean checked = false;

		public SQLResult() {
		}

		public SQLResult(String queryId, List result, String status, String message) {
			this.queryId = queryId;
			this.result = result;
			this.status = status;
			this.message = message;
		}

		public String getQueryId() {
			return queryId;
		}

		public SQLResult setQueryId(String queryId) {
			this.queryId = queryId;
			return this;
		}

		public List getResult() {
			return result;
		}

		public SQLResult setResult(List result) {
			this.result = result;
			return this;
		}

		public String getStatus() {
			return status;
		}

		public SQLResult setStatus(String status) {
			this.status = status;
			return this;
		}

		public String getMessage() {
			return message;
		}

		public SQLResult setMessage(String message) {
			this.message = message;
			return this;
		}

		public String getGenerateTime() {
			return generateTime;
		}

		public SQLResult setGenerateTime(String generateTime) {
			this.generateTime = generateTime;
			return this;
		}

		public SQLResult setChecked(boolean checked) {
			this.checked = checked;
			return this;
		}

		@Override
		public String toString() {
			return "SQLResult{" +
					"queryId='" + queryId + '\'' +
					", result='" + result + '\'' +
					", status='" + status + '\'' +
					", message='" + message + '\'' +
					", generateTime='" + generateTime + '\'' +
					", checked=" + checked +
					'}';
		}
	}

}
