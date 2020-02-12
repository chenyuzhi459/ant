package io.sugo.services.query.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DruidResult<T> {
    T event;

    @JsonCreator
    public DruidResult(
            @JsonProperty("event") T event) {
        this.event = event;
    }

    public T getEvent() {
        return event;
    }
}
