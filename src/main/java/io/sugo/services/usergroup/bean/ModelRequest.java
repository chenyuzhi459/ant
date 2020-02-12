package io.sugo.services.usergroup.bean;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.sugo.services.usergroup.bean.lifecycle.LifeCycleRequestBean;
import io.sugo.services.usergroup.bean.rfm.RFMRequestBean;
import io.sugo.services.usergroup.bean.valuetier.ValueTierRequestBean;
import io.sugo.services.usergroup.model.valuetier.ValueTierManager;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(name = RFMRequestBean.TYPE, value = RFMRequestBean.class),
        @JsonSubTypes.Type(name = LifeCycleRequestBean.TYPE, value = LifeCycleRequestBean.class),
        @JsonSubTypes.Type(name = ValueTierRequestBean.TYPE, value = ValueTierRequestBean.class)
})
public interface ModelRequest {
    String getRequestId();
    String getCallbackUrl();
    String getType();
}
