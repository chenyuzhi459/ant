package io.sugo.services.usergroup.bean.valuetier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.services.usergroup.bean.ModelRequest;
import io.sugo.services.usergroup.bean.rfm.DataBean;
import io.sugo.services.usergroup.bean.rfm.RFMDimensions;

import java.util.Comparator;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class ValueTierRequestBean implements ModelRequest {
    public static final String TYPE = "valueTier";
    private List<DataBean> datasets;
    private RFMDimensions dimensions;
    private List<ValueTier> params;
    private RedisDataIOFetcher redisConfig;
    private String requestId;
    private String callbackUrl;

    @JsonCreator
    public ValueTierRequestBean(
            @JsonProperty("datasets") List<DataBean> datasets,
            @JsonProperty("requestId") String requestId,
            @JsonProperty("dimensions") RFMDimensions dimensions,
            @JsonProperty("params") List<ValueTier> params,
            @JsonProperty("redisConfig") RedisDataIOFetcher redisConfig,
            @JsonProperty("callbackUrl") String callbackUrl) {
        this.datasets = datasets;
        this.requestId = requestId;
        this.dimensions = dimensions;
        params.sort(Comparator.comparing(ValueTier::getId));
        this.params = params;
        this.redisConfig = redisConfig;
        this.callbackUrl = callbackUrl;
    }

    @JsonProperty
    public List<DataBean> getDatasets() {
        return datasets;
    }

    @JsonProperty
    public RFMDimensions getDimensions() {
        return dimensions;
    }

    @JsonProperty
    public List<ValueTier> getParams() {
        return params;
    }

    @JsonProperty
    @Override
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty
    @Override
    public String getCallbackUrl() {
        return callbackUrl;
    }

    @JsonProperty
    @Override
    public String getType() {
        return TYPE;
    }

    @JsonProperty
    public RedisDataIOFetcher getRedisConfig() {
        return redisConfig;
    }
}
