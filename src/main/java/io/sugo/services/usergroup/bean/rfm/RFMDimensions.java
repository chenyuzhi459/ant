package io.sugo.services.usergroup.bean.rfm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RFMDimensions {
    //用户Id维度
    private String userIdKey;
    //购买时间维度
    private String buyTimeKey;
    //购买金额维度
    private String buyTimeAmount;

    @JsonCreator
    public RFMDimensions(
            @JsonProperty("userIdKey") String userIdKey,
            @JsonProperty("buyTimeKey") String buyTimeKey,
            @JsonProperty("buyTimeAmount") String buyTimeAmount)
    {
        Preconditions.checkNotNull(userIdKey, "userIdKey can not be null.");
        Preconditions.checkNotNull(buyTimeKey, "buyTimeKey can not be null.");
        Preconditions.checkNotNull(buyTimeAmount, "buyTimeAmount can not be null.");
        this.userIdKey = userIdKey;
        this.buyTimeKey = buyTimeKey;
        this.buyTimeAmount = buyTimeAmount;
    }

    public String getUserIdKey() {
        return userIdKey;
    }

    public String getBuyTimeKey() {
        return buyTimeKey;
    }

    public String getBuyTimeAmount() {
        return buyTimeAmount;
    }
}