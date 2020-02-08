package io.sugo.services.usergroup.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.sugo.services.pathanalysis.dto.PathAnalysisDto.SCAN_QUERY_TIMOUT_MILLIS;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanQuery {
    String queryType = "lucene_scan";
    String dataSource;
    String resultFormat = "compactedList";
    int batchSize;
    int limit;
    List<String> columns;
    Object intervals;
    Object filter;
    Map<String, Object> context;

    //用于路径分析
    public ScanQuery() {
        resultFormat = "compactedList";
        columns = new ArrayList<>();
        intervals = new ArrayList<>();
        context = Maps.newHashMap();
        context.put("timeout", SCAN_QUERY_TIMOUT_MILLIS);
    }

    //用于接口构造
    @JsonCreator
    public ScanQuery(
            @JsonProperty("dataSource") String dataSource,
            @JsonProperty("resultFormat") String resultFormat,
            @JsonProperty("batchSize") Integer batchSize,
            @JsonProperty("limit") Integer limit,
            @JsonProperty("columns") List<String> columns,
            @JsonProperty("intervals") Object intervals,
            @JsonProperty("filter") Object filter,
            @JsonProperty("context") Map<String, Object> context) {
        this.queryType = queryType;
        this.dataSource = dataSource;
        this.resultFormat = resultFormat == null ? "compactedList" : resultFormat;
        this.batchSize = batchSize;
        this.limit = limit;
        this.columns = columns;
        this.intervals = intervals;
        this.filter = filter;
        this.context = context;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getResultFormat() {
        return resultFormat;
    }

    public void setResultFormat(String resultFormat) {
        this.resultFormat = resultFormat;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public Object getIntervals() {
        return intervals;
    }

    public void setIntervals(Object intervals) {
        this.intervals = intervals;
    }

    public Object getFilter() {
        return filter;
    }

    public void setFilter(Object filter) {
        this.filter = filter;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
}
