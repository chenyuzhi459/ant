package io.sugo.services.usergroup.bean.lifecycle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.services.usergroup.bean.rfm.DataBean;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class DataSetSpec {
    private DataBean uindexDataBean;
    private DataBean tradeDataBean;
    private DataBean behaviorDataBean;

    @JsonCreator
    public DataSetSpec(
            @JsonProperty("uindexData") DataBean uindexDataBean,
            @JsonProperty("tradeData") DataBean tradeDataBean,
            @JsonProperty("behaviorData") DataBean behaviorDataBean) {
        this.uindexDataBean = uindexDataBean;
        this.tradeDataBean = tradeDataBean;
        this.behaviorDataBean = behaviorDataBean;
    }

    @JsonProperty("uindexData")
    public DataBean getUindexDataBean() {
        return uindexDataBean;
    }

    @JsonProperty("tradeData")
    public DataBean getTradeDataBean() {
        return tradeDataBean;
    }

    @JsonProperty("behaviorData")
    public DataBean getBehaviorDataBean() {
        return behaviorDataBean;
    }
}
