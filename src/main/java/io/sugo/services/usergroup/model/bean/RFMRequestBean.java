package io.sugo.services.usergroup.model.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.sugo.services.usergroup.bean.rfm.DataBean;
import io.sugo.services.usergroup.bean.rfm.RFMDimensions;
import io.sugo.services.usergroup.bean.rfm.RFMParams;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class RFMRequestBean {
    private List<DataBean> dataSet;
    private RFMDimensions dimensions;
    private RFMParams params;
    private String requestId;
    private String callbackUrl;
    private Object redisConfig;

    @JsonCreator
    public RFMRequestBean(
            @JsonProperty("dataSet") List<DataBean> dataSet,
            @JsonProperty("dimensions") RFMDimensions dimensions,
            @JsonProperty("params") RFMParams params,
            @JsonProperty("redisConfig") Object redisConfig,
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

    public List<DataBean> getDataSet() {
        return dataSet;
    }

    public RFMDimensions getDimensions() {
        return dimensions;
    }

    public RFMParams getParams() {
        return params;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }
}
