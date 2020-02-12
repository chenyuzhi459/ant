package io.sugo.services.query.filter;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Filter extends FieldType {
    String dimension;

    @JsonProperty
    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }
}
