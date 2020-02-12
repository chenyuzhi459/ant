package io.sugo.services.usergroup.bean.lifecycle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.utils.ModelUtil;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;
import io.sugo.services.query.aggregator.Aggregation;
import io.sugo.services.query.aggregator.FilterAggregation;
import io.sugo.services.query.filter.AndFilter;
import io.sugo.services.query.filter.BetweenFilter;
import io.sugo.services.query.filter.FieldType;
import io.sugo.services.query.filter.NotNullFilter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class Stage {
    private Integer stageId;
    private String name;
    private RangeInfo historyRange;
    private RangeInfo stageRange;
    private String intervalStart;
    private String intervalEnd;

    @JsonCreator
    public Stage(
            @JsonProperty("stageId") Integer stageId,
            @JsonProperty("name") String name,
            @JsonProperty("historyAccRange") RangeInfo historyRange,
            @JsonProperty("stageAccRange") RangeInfo stageRange,
            @JsonProperty("intervalStart") String intervalStart,
            @JsonProperty("intervalEnd") String intervalEnd) {
        this.stageId = stageId;
        this.name = name;
        this.historyRange = historyRange;
        this.stageRange = stageRange;
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
    }

    public FilterAggregation buildStageAggregation(String dimension){
        Aggregation countAgg = new Aggregation();
        countAgg.setName(getAggregatorName(dimension));
        countAgg.setType("lucene_count");
        AndFilter andFilter = new AndFilter();
        List<FieldType> fieldTypes = andFilter.getFields();
        //非null过滤
        NotNullFilter notNullField = new NotNullFilter();
        notNullField.setDimension(dimension);
        fieldTypes.add(notNullField);

        //时间过滤
       BetweenFilter betweenField = new BetweenFilter();
        betweenField.setDimension(dimension);
        betweenField.setLower(intervalStart);
        betweenField.setUpper(intervalEnd);
        fieldTypes.add(betweenField);

        return new FilterAggregation(countAgg, andFilter);
    }

    public String getAggregatorName(String dimension){
        return String.format("%s_%s_%s", dimension, intervalStart, intervalEnd);
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public Integer getStageId() {
        return stageId;
    }

    @JsonProperty("historyAccRange")
    public RangeInfo getHistoryRange() {
        return historyRange;
    }

    @JsonProperty("stageAccRange")
    public RangeInfo getStageRange() {
        return stageRange;
    }

    @JsonProperty
    public String getIntervalStart() {
        return intervalStart;
    }

    @JsonProperty
    public String getIntervalEnd() {
        return intervalEnd;
    }


    @JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
    public static  class RangeInfo {
        private Long max;
        private Long min;

        @JsonCreator
        public RangeInfo(@JsonProperty("max") Long max, @JsonProperty("min") Long min) {
            this.max = max;
            this.min = min;
        }

        @JsonProperty
        public Long getMax() {
            return max;
        }

        @JsonProperty
        public Long getMin() {
            return min;
        }
    }
}
