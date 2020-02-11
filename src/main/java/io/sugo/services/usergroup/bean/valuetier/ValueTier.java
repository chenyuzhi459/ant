package io.sugo.services.usergroup.bean.valuetier;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValueTier {
    private Integer id;
    private String name;
    private Double percent;

    public ValueTier(Integer id, String name, Double percent) {
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
