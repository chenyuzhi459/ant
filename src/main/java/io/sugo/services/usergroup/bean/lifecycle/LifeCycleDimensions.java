package io.sugo.services.usergroup.bean.lifecycle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LifeCycleDimensions {
    //用户Id维度
    private String userIdKey;
    //购买时间维度--计算订单成交行为
    private String buyTimeKey;
    //行为时间维度--计算交互行为
    private String behaviorTimeKey;
    //注册时间维度
    private String registerTimeKey;

    @JsonCreator
    public LifeCycleDimensions(
            @JsonProperty("userIdKey") String userIdKey,
            @JsonProperty("registerTimeKey") String registerTimeKey,
            @JsonProperty("buyTimeKey") String buyTimeKey,
            @JsonProperty("behaviorTimeKey") String behaviorTimeKey)
    {
        Preconditions.checkNotNull(userIdKey, "userIdKey can not be null.");
        Preconditions.checkNotNull(buyTimeKey, "buyTimeKey can not be null.");
        Preconditions.checkNotNull(behaviorTimeKey, "buyTimebuyAmountKeyAmount can not be null.");
        this.userIdKey = userIdKey;
        this.registerTimeKey = registerTimeKey;
        this.buyTimeKey = buyTimeKey;
        this.behaviorTimeKey = behaviorTimeKey;
    }

    @JsonProperty
    public String getUserIdKey() {
        return userIdKey;
    }

    @JsonProperty
    public String getBuyTimeKey() {
        return buyTimeKey;
    }

    @JsonProperty
    public String getBehaviorTimeKey() {
        return behaviorTimeKey;
    }

    @JsonProperty
    public String getRegisterTimeKey() {
        return registerTimeKey;
    }
}