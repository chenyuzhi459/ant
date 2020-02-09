package io.sugo.services.usergroup.bean.lifecycle;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.sugo.services.usergroup.bean.rfm.DataBean;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class DataSetSpec {
    private DataBean uindexDataBean;
    private DataBean tradeDataBean;
    private DataBean behaviorDataBean;

    public DataSetSpec(DataBean uindexDataBean, DataBean tradeDataBean, DataBean behaviorDataBean) {
        this.uindexDataBean = uindexDataBean;
        this.tradeDataBean = tradeDataBean;
        this.behaviorDataBean = behaviorDataBean;
    }

    public DataBean getUindexDataBean() {
        return uindexDataBean;
    }

    public DataBean getTradeDataBean() {
        return tradeDataBean;
    }

    public DataBean getBehaviorDataBean() {
        return behaviorDataBean;
    }
}
