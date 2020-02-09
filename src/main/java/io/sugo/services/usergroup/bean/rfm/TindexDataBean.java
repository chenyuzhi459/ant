package io.sugo.services.usergroup.bean.rfm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.metamx.common.logger.Logger;
import io.sugo.common.utils.HttpClinetUtil;
import io.sugo.services.usergroup.model.rfm.RFMManager;
import io.sugo.services.usergroup.model.rfm.RFMModel;
import io.sugo.services.usergroup.query.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class TindexDataBean extends DataBean{

    public static final String TYPE = "tindex";
    @JsonCreator
    public TindexDataBean(
            @JsonProperty("broker") String broker,
            @JsonProperty("query") Query query)
    {
        super(broker, query);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
