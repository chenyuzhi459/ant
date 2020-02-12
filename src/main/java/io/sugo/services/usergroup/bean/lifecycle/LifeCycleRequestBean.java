package io.sugo.services.usergroup.bean.lifecycle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.services.usergroup.bean.ModelRequest;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class LifeCycleRequestBean implements ModelRequest {
    public static final String TYPE = "lifeCycle";
    private String requestId;
    private DataSpec dataSpec;
    private LifeCycleDimensions dimensions;
    private StagesSpec stagesSpec;
    private RedisDataIOFetcher redisConfig;
    private String callbackUrl;

    @JsonCreator
    public LifeCycleRequestBean(
            @JsonProperty("requestId") String requestId,
            @JsonProperty("datasets") DataSpec dataSpec,
            @JsonProperty("dimensions") LifeCycleDimensions dimensions,
            @JsonProperty("params") StagesSpec stagesSpec,
            @JsonProperty("redisConfig") RedisDataIOFetcher redisConfig,
            @JsonProperty("callbackUrl") String callbackUrl) {
        this.requestId = requestId;
        this.dataSpec = dataSpec;
        this.dimensions = dimensions;
        this.stagesSpec = stagesSpec;
        this.redisConfig = redisConfig;
        this.callbackUrl = callbackUrl;
    }

    @JsonProperty
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty
    public DataSpec getDataSpec() {
        return dataSpec;
    }

    @JsonProperty
    public LifeCycleDimensions getDimensions() {
        return dimensions;
    }

    @JsonProperty("params")
    public StagesSpec getStagesSpec() {
        return stagesSpec;
    }

    @JsonProperty
    public RedisDataIOFetcher getRedisConfig() {
        return redisConfig;
    }

    @JsonProperty
    public String getCallbackUrl() {
        return callbackUrl;
    }

    @JsonProperty("type")
    public String getType() {
        return TYPE;
    }
}
