package io.sugo.services.usergroup.bean.rfm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl= DefaultRFMParams.class)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(name = "default", value = DefaultRFMParams.class),
        @JsonSubTypes.Type(name = "custom", value = CustomRFMParams.class)
})
public interface RFMParams<T> {
    @JsonProperty("R")
    public T getR();
    @JsonProperty("F")
    public T getF();
    @JsonProperty("M")
    public T getM();
}