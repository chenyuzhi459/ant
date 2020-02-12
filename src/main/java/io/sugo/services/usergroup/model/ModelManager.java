package io.sugo.services.usergroup.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.redis.RedisClientWrapper;
import io.sugo.common.redis.RedisInfo;
import io.sugo.common.utils.*;
import io.sugo.server.http.Configure;
import io.sugo.services.cache.Caches;
import io.sugo.services.usergroup.bean.ModelRequest;
import io.sugo.services.usergroup.bean.lifecycle.LifeCycleRequestBean;
import io.sugo.services.usergroup.bean.rfm.RFMRequestBean;
import io.sugo.services.usergroup.bean.valuetier.ValueTierRequestBean;
import io.sugo.services.usergroup.model.lifecycle.LifeCycleManager;
import io.sugo.services.usergroup.model.rfm.RFMManager;
import io.sugo.services.usergroup.model.valuetier.ValueTierManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.util.Map;
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
    private LifeCycleManager lifeCycleManager;
    private ValueTierManager valueTierManager;
    public static Map<String, Object> RESULT_MAP = new CapacityMap<>(1);

    @Inject
    public ModelManager(@Json ObjectMapper jsonMapper,
                        Caches.RedisClientCache redisClientCache,
                        RFMManager rfmManager,
                        LifeCycleManager lifeCycleManager,
                        ValueTierManager valueTierManager) {
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
        this.lifeCycleManager = lifeCycleManager;
        this.valueTierManager = valueTierManager;
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
                ModelRequest requestBean = this.jsonMapper.readValue(requestBodyStr, ModelRequest.class);
                if(requestBean == null){
                    return;
                }

                String id = requestBean.getRequestId();
                log.info(String.format("found model request, id: %s", id ));

                Object result = handleRequest(requestBean);
                ModelResult modelResult = new ModelResult(id, result);
                log.info("finish request, id:  " + id);
                RESULT_MAP.put(id, modelResult);
                String callBackUrl = requestBean.getCallbackUrl();
                if(callBackUrl != null){
                    HttpClinetUtil.post(callBackUrl, this.jsonMapper.writeValueAsString(modelResult));
                    log.info("finished callback for request, id: " + id);
                }
//                this.doMultiUserGroupOperationV2(requestBean);
            }

        } finally {
            redisClientCache.releaseRedisClient(systemRedisInfo, systemRedisClient);
        }

    }

    private Object handleRequest(ModelRequest request){
        if(request instanceof RFMRequestBean){
            return rfmManager.handle((RFMRequestBean)request);
        }else if(request instanceof LifeCycleRequestBean){
            return lifeCycleManager.handle((LifeCycleRequestBean)request);
        } else if(request instanceof ValueTierRequestBean){
            return valueTierManager.handle((ValueTierRequestBean)request);
        } else {
            throw new UnsupportedOperationException();
        }
    }



    public void addToRedisQueue(ModelRequest requestBean) throws Exception {
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

    private class ModelResult{
        private String requestId;
        private Object data;

        public ModelResult(String requestId, Object data) {
            this.requestId = requestId;
            this.data = data;
        }

        @JsonProperty
        public String getRequestId() {
            return requestId;
        }

        @JsonProperty
        public Object getData() {
            return data;
        }
    }
}
