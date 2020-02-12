package io.sugo.services.query.dimension;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Dimension {
    public static final String USER_ID = "userId";
    String type = "default";
    String dimension;
    String outputName = USER_ID;

    @JsonProperty
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty
    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    @JsonProperty
    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }
}
