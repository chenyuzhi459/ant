package io.sugo.services.usergroup.bean.rfm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ScanQueryResult {
    String segmentId;
    List<String> columns;
    List<List> events;

    @JsonCreator
    public ScanQueryResult(
            @JsonProperty("segmentId")String segmentId,
            @JsonProperty("columns")  List<String> columns,
            @JsonProperty("events") List<List> events) {
        this.segmentId = segmentId;
        this.columns = columns;
        this.events = events;
    }

    public  List<String> getColumns() {
        return columns;
    }

    public List<List> getEvents() {
        return events;
    }
}
