package io.sugo.services.usergroup.bean.lifecycle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.redis.RedisDataIOFetcher;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class LifeCycleRequestBean {
    private String requestId;
    private DataSpec dataSpec;
    private LifeCycleDimensions dimensions;
    private StagesSpec stagesSpec;
    private RedisDataIOFetcher redisConfig;

    @JsonCreator
    public LifeCycleRequestBean(
            @JsonProperty("requestId") String requestId,
            @JsonProperty("dataSpec") DataSpec dataSpec,
            @JsonProperty("dimensions") LifeCycleDimensions dimensions,
            @JsonProperty("stagesSpec") StagesSpec stagesSpec,
            @JsonProperty("redisConfig") RedisDataIOFetcher redisConfig) {
        this.requestId = requestId;
        this.dataSpec = dataSpec;
        this.dimensions = dimensions;
        this.stagesSpec = stagesSpec;
        this.redisConfig = redisConfig;
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

    @JsonProperty
    public StagesSpec getStagesSpec() {
        return stagesSpec;
    }

    public RedisDataIOFetcher getRedisConfig() {
        return redisConfig;
    }
}
