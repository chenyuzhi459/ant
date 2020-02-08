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
    private String buyAmountKey;

    @JsonCreator
    public RFMDimensions(
            @JsonProperty("userIdKey") String userIdKey,
            @JsonProperty("buyTimeKey") String buyTimeKey,
            @JsonProperty("buyAmountKey") String buyAmountKey)
    {
        Preconditions.checkNotNull(userIdKey, "userIdKey can not be null.");
        Preconditions.checkNotNull(buyTimeKey, "buyTimeKey can not be null.");
        Preconditions.checkNotNull(buyAmountKey, "buyTimebuyAmountKeyAmount can not be null.");
        this.userIdKey = userIdKey;
        this.buyTimeKey = buyTimeKey;
        this.buyAmountKey = buyAmountKey;
    }

    public String getUserIdKey() {
        return userIdKey;
    }

    public String getBuyTimeKey() {
        return buyTimeKey;
    }

    public String getBuyAmountKey() {
        return buyAmountKey;
    }
}