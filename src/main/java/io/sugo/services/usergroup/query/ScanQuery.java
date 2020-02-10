package io.sugo.services.usergroup.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import io.sugo.common.redis.RedisDataIOFetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.sugo.services.pathanalysis.dto.PathAnalysisDto.SCAN_QUERY_TIMOUT_MILLIS;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class ScanQuery implements Query{
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
        this.dataSource = dataSource;
        this.resultFormat = resultFormat == null ? "compactedList" : resultFormat;
        this.batchSize = batchSize;
        this.limit = limit;
        this.columns = columns;
        this.intervals = intervals;
        this.filter = filter;
        this.context = context;
    }

    @JsonProperty
    public String getQueryType() {
        return queryType;
    }

    @JsonProperty
    public String getDataSource() {
        return dataSource;
    }

    @JsonProperty
    public String getResultFormat() {
        return resultFormat;
    }

    @JsonProperty
    public int getBatchSize() {
        return batchSize;
    }

    @JsonProperty
    public int getLimit() {
        return limit;
    }

    @JsonProperty
    public List<String> getColumns() {
        return columns;
    }

    @JsonProperty
    public Object getIntervals() {
        return intervals;
    }

    @JsonProperty
    public Object getFilter() {
        return filter;
    }

    @JsonProperty
    public Map<String, Object> getContext() {
        return context;
    }

    public ScanQuery setDataSource(String dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public ScanQuery setFilter(Object filter) {
        this.filter = filter;
        return this;
    }

    public ScanQuery setIntervals(Object intervals) {
        this.intervals = intervals;
        return this;
    }

    public ScanQuery setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public ScanQuery setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public RedisDataIOFetcher getDataConfig() {
        throw new UnsupportedOperationException("Not support to getDataConfig for scan query");
    }
}
