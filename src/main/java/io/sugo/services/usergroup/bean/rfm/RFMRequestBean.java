package io.sugo.services.usergroup.bean.rfm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.services.usergroup.bean.ModelRequest;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class RFMRequestBean implements ModelRequest {
    public static final String TYPE = "rfm";
    private List<DataBean> dataSet;
    private RFMDimensions dimensions;
    private RFMParams params;
    private String requestId;
    private String callbackUrl;
    private RedisDataIOFetcher redisConfig;

    @JsonCreator
    public RFMRequestBean(
            @JsonProperty("datasets") List<DataBean> dataSet,
            @JsonProperty("dimensions") RFMDimensions dimensions,
            @JsonProperty("params") RFMParams params,
            @JsonProperty("redisConfig") RedisDataIOFetcher redisConfig,
            @JsonProperty("requestId")  String requestId,
            @JsonProperty("callbackUrl") String callbackUrl)
    {
        Preconditions.checkArgument(dataSet != null && !dataSet.isEmpty(), "dataSet can not be null or empty.");
        Preconditions.checkNotNull(dimensions, "dimensions can not be null.");
        Preconditions.checkNotNull(params, "params can not be null.");
        this.dataSet = dataSet;
        this.dimensions = dimensions;
        this.params = params;
        this.requestId = requestId;
        this.callbackUrl = callbackUrl;
        this.redisConfig = redisConfig;
    }

    @JsonProperty("datasets")
    public List<DataBean> getDataSet() {
        return dataSet;
    }

    @JsonProperty
    public RFMDimensions getDimensions() {
        return dimensions;
    }

    @JsonProperty
    public RFMParams getParams() {
        return params;
    }

    @JsonProperty
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty
    public String getCallbackUrl() {
        return callbackUrl;
    }

    @JsonProperty
    public RedisDataIOFetcher getRedisConfig() {
        return redisConfig;
    }

    @JsonProperty
    public String getType() {
        return TYPE;
    }
}
