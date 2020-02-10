package io.sugo.services.usergroup.bean;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.sugo.services.usergroup.bean.lifecycle.LifeCycleRequestBean;
import io.sugo.services.usergroup.bean.rfm.RFMRequestBean;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(name = RFMRequestBean.TYPE, value = RFMRequestBean.class),
        @JsonSubTypes.Type(name = LifeCycleRequestBean.TYPE, value = LifeCycleRequestBean.class)
})
public interface ModelRequest {
    String getRequestId();
    String getCallbackUrl();
    String getType();
}
