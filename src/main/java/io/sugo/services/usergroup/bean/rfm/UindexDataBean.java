package io.sugo.services.usergroup.bean.rfm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.sugo.services.usergroup.query.Query;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class UindexDataBean extends DataBean{
    public static final String TYPE = "uindex";
    @JsonCreator
    public UindexDataBean(
            @JsonProperty("broker") String broker,
            @JsonProperty("query") Query query) {
        super(broker, query);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void getData() {

    }


}
