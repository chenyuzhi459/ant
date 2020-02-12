package io.sugo.services.usergroup.bean.valuetier;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class ValueTier {
    private Integer id;
    private String name;
    private Double percent;

    public ValueTier(
            @JsonProperty("id") Integer id,
            @JsonProperty("name") String name,
            @JsonProperty("percent") Double percent) {
        this.id = id;
        this.name = name;
        this.percent = percent;
    }

    @JsonProperty
    public Integer getId() {
        return id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public Double getPercent() {
        return percent;
    }
}
