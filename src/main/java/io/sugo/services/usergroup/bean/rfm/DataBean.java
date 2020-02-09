package io.sugo.services.usergroup.bean.rfm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.sugo.services.usergroup.query.Query;

public class DataBean {
    protected Query query;      //maybe scanQuery or groupByQuery for rfm
    protected String broker;
    public  String type;
    public static final String TINDEX_TYPE = "tindex";
    public static final String UINDEX_TYPE = "uindex";

    @JsonCreator
    public DataBean(
            @JsonProperty("type") String type,
            @JsonProperty("broker") String broker,
            @JsonProperty("query") Query query)
    {
        Preconditions.checkNotNull(query, "query can not be null.");
        Preconditions.checkNotNull(broker, "broker can not be null.");
        this.query = query;
        this.broker = broker;
        this.type = type;
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
    public String getType(){
        return type;
    }

}
