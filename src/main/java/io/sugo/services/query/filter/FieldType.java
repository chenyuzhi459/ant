package io.sugo.services.query.filter;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldType {
    protected String type;

    @JsonProperty
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
