package io.sugo.services.usergroup.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.redis.RedisClientWrapper;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.common.redis.RedisInfo;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.common.utils.*;
import io.sugo.server.http.Configure;
import io.sugo.services.cache.Caches;
import io.sugo.services.usergroup.bean.rfm.CustomRFMParams;
import io.sugo.services.usergroup.bean.rfm.DefaultRFMParams;
import io.sugo.services.usergroup.bean.rfm.RFMParams;
import io.sugo.services.usergroup.bean.rfm.RFMRequestBean;
import io.sugo.services.usergroup.model.rfm.QuantileModel;
import io.sugo.services.usergroup.model.rfm.RFMGroup;
import io.sugo.services.usergroup.model.rfm.RFMManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.sugo.common.utils.Constants.SYSTEM_PROPS;
import static io.sugo.common.utils.Constants.Sys.*;
import static io.sugo.common.utils.Constants.UserGroup.CONSUMER_RUN_INTERVAL;
import static io.sugo.common.utils.Constants.UserGroup.CONSUMER_THREAD_SIZE;

public class ModelManager implements AntService {
    private static final Logger log = LogManager.getLogger(ModelManager.class);

    @Inject
    private static Configure configure;
    @Inject @Named(Constants.UserGroup.MODEL_QUEUE_REDIS_KEY)
    public static  String QUEUE_REDIS_KEY;
    @Inject @Named(Constants.UserGroup.QUERY_RESULT_REDIS_KEY)
    public static  String RESULT_REDIS_KEY ;
    // Use to synchronize start() and stop(). These methods should be synchronized to prevent from being called at the
    // same time if two different threads are calling them. This might be possible if a druid coordinator gets and drops
    // leadership repeatedly in quick succession.
    private final Object lock = new Object();
    private volatile ListeningScheduledExecutorService consumerExec = null;
    private volatile ListenableFuture<?> consumerFuture = null;
    private volatile boolean started;
    private final RedisInfo systemRedisInfo;
    private final ObjectMapper jsonMapper;
    private final Caches.RedisClientCache redisClientCache;
    private RFMManager rfmManager;
    public static Map<String, Object> RESULT_MAP = new CapacityMap<>(1);

    @Inject
    public ModelManager(@Json ObjectMapper jsonMapper,
                        Caches.RedisClientCache redisClientCache,
                        RFMManager rfmManager) {
        log.info("create new ModelManager...");
        this.jsonMapper = jsonMapper;
        this.redisClientCache = redisClientCache;
        this.systemRedisInfo =  new RedisInfo(
                configure.getProperty(SYSTEM_PROPS, REDIS_HOST_AND_PORT),
                configure.getBoolean(SYSTEM_PROPS, REDIS_CLUSTER_MODE,false),
                configure.getBoolean(SYSTEM_PROPS, REDIS_SENTINEL_MODE,false),
                configure.getProperty(SYSTEM_PROPS, REDIS_MASTER_NAME,null),
                configure.getProperty(SYSTEM_PROPS, REDIS_PASSWORD,null));
        this.rfmManager = rfmManager;

    }

    public void start(){
        synchronized (lock) {
            if (started) {
                return;
            }
            consumerExec = MoreExecutors.listeningDecorator(ExecUtil.scheduledMutilThread(
                    configure.getInt(SYSTEM_PROPS, CONSUMER_THREAD_SIZE, 5),
                    "ModelManager-Consumer-Exec--%d"));

            consumerFuture = consumerExec.scheduleWithFixedDelay(() ->{
                        try {
                            consume();
                        } catch (Exception e) {
                            log.error("ModelManager consumer error.",e);
                        }
                    },
                    0,
                    configure.getInt(SYSTEM_PROPS, CONSUMER_RUN_INTERVAL, 5),
                    TimeUnit.SECONDS);

            log.info("start consumer for ModelManager...");
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
            log.info("stop  producer and consumer for ModelManager...");
            started = false;
        }
    }


    private void consume() throws Exception{

        RedisClientWrapper systemRedisClient = null;
        try {
            //jedis实例非线程安全,所以每个线程从缓存中获取一个实例
            systemRedisClient = redisClientCache.getRedisClient(systemRedisInfo);
            String requestBodyStr = null;
            while(( requestBodyStr = systemRedisClient.rpop(QUEUE_REDIS_KEY)) != null){
                RFMRequestBean requestBean = this.jsonMapper.readValue(requestBodyStr, RFMRequestBean.class);
                if(requestBean == null){
                    return;
                }

                String id = requestBean.getRequestId();
                log.info(String.format("found model request, id: %s", id ));

                Object result = handleRequest(requestBean);
                log.info("finish request, id:  " + id);
                String callBackUrl = requestBean.getCallbackUrl();
                if(callBackUrl != null){
                    HttpClinetUtil.post(callBackUrl, this.jsonMapper.writeValueAsString(result));
                }
                RESULT_MAP.put(id, result);
//                this.doMultiUserGroupOperationV2(requestBean);
            }

        } finally {
            redisClientCache.releaseRedisClient(systemRedisInfo, systemRedisClient);
        }

    }

    private QuantileModel handleRequest(RFMRequestBean requestBean){
        RFMParams params = requestBean.getParams();
        QuantileModel quantileModel = null;
        if (params instanceof DefaultRFMParams){
            DefaultRFMParams defaultRFMParams = (DefaultRFMParams)params;
            quantileModel = rfmManager.getDefaultQuantileModel(requestBean, defaultRFMParams.getR(), defaultRFMParams.getF(), defaultRFMParams.getM());
        }else {
            CustomRFMParams customRFMParams = (CustomRFMParams)params;
            quantileModel =  rfmManager.getCustomizedQuantileModel(requestBean, customRFMParams.getR(), customRFMParams.getF(), customRFMParams.getM());
        }
        splitRFMToUserGroup(requestBean, quantileModel);

        return quantileModel;
    }

    private void splitRFMToUserGroup(RFMRequestBean requestBean, QuantileModel quantileModel){
        RedisDataIOFetcher redisConfig = requestBean.getRedisConfig();
        UserGroupSerDeserializer userGroupSerDeserializer = new UserGroupSerDeserializer(redisConfig);
        List<String> userId = null;
        synchronized (redisConfig){
            for(RFMGroup group : quantileModel.getGroups()){
                redisConfig.setGroupId(group.getGroupId());
                userId = group.getUserIdList();
                UserGroupUtil.writeDataToRedis(userGroupSerDeserializer, ImmutableSet.copyOf(userId));
                //释放内存
                userId.clear();
                log.info(String.format("write data to usergroup: %s finished.", group.getGroupId()));

            }
        }
    }

    public void addToRedisQueue(RFMRequestBean requestBean) throws Exception {
        RedisClientWrapper systemRedisClient = null;
        try {
            systemRedisClient = redisClientCache.getRedisClient(systemRedisInfo);
            String requestStr = this.jsonMapper.writeValueAsString(requestBean);
            systemRedisClient.lpush(QUEUE_REDIS_KEY,requestStr);
            log.info(String.format("add model request to queue success, id = %s", requestBean.getRequestId()));
        } finally {
            redisClientCache.releaseRedisClient(systemRedisInfo,systemRedisClient);
        }
    }

    public Object fetchResult(String requstId){
        return RESULT_MAP.get(requstId);
    }
}
