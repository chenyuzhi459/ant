package io.sugo.services.usergroup.bean.rfm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import io.sugo.services.usergroup.query.Query;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl= TindexDataBean.class)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(name = "uindex", value = UindexDataBean.class),
        @JsonSubTypes.Type(name = "tindex", value = TindexDataBean.class)
})
public abstract class DataBean {
    protected Query query;      //maybe scanQuery or groupByQuery for rfm
    protected String broker;

    @JsonCreator
    public DataBean(
            @JsonProperty("broker") String broker,
            @JsonProperty("query") Query query)
    {
        Preconditions.checkNotNull(query, "query can not be null.");
        Preconditions.checkNotNull(broker, "broker can not be null.");
        this.query = query;
        this.broker = broker;
    }

    @JsonProperty
    public Query getQuery() {
        return query;
    }

    @JsonProperty
    public String getBroker() {
        return broker;
    }

    @JsonProperty
    public abstract String getType();

}
