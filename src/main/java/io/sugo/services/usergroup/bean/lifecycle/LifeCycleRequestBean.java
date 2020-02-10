package io.sugo.services.usergroup.bean.lifecycle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.redis.RedisDataIOFetcher;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class LifeCycleRequestBean {
    private String requestId;
    private DataSetSpec datasets;
    private LifeCycleDimensions dimensions;
    private StagesSpec stages;
    private RedisDataIOFetcher redisConfig;

    @JsonCreator
    public LifeCycleRequestBean(
            @JsonProperty("requestId") String requestId,
            @JsonProperty("datasets")  DataSetSpec datasets,
            @JsonProperty("dimensions") LifeCycleDimensions dimensions,
            @JsonProperty("stages") StagesSpec stages,
            @JsonProperty("redisConfig") RedisDataIOFetcher redisConfig) {
        this.requestId = requestId;
        this.datasets = datasets;
        this.dimensions = dimensions;
        this.stages = stages;
        this.redisConfig = redisConfig;
    }

    @JsonProperty
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty
    public DataSetSpec getDatasets() {
        return datasets;
    }

    @JsonProperty
    public LifeCycleDimensions getDimensions() {
        return dimensions;
    }

    @JsonProperty
    public StagesSpec getStages() {
        return stages;
    }

    public RedisDataIOFetcher getRedisConfig() {
        return redisConfig;
    }
}
