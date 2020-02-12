package io.sugo.services.usergroup.bean.lifecycle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.utils.ModelUtil;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;
import io.sugo.services.query.aggregator.Aggregation;
import io.sugo.services.query.aggregator.FilterAggregation;
import io.sugo.services.query.filter.AndFilter;
import io.sugo.services.query.filter.FieldType;
import io.sugo.services.query.filter.NotNullFilter;

import java.util.Comparator;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class StagesSpec {
    public static final String HISTORY_AGG_OUTPUT_NAME = "history";
    private String historyStart;
    private String historyEnd;
    private List<Stage> stages;

    @JsonCreator
    public StagesSpec(
            @JsonProperty("historyStart") String historyStart,
            @JsonProperty("historyEnd") String historyEnd,
            @JsonProperty("stages") List<Stage> stages) {
        this.historyStart = historyStart;
        this.historyEnd = historyEnd;
        this.stages = stages;
        this.stages.sort(Comparator.comparing(Stage::getStageId));
    }

    public FilterAggregation buildHistoryAggregator(String dimension) {
        Aggregation countAgg = new Aggregation();
        countAgg.setName(HISTORY_AGG_OUTPUT_NAME);
        countAgg.setType("lucene_count");
        AndFilter andFilter = new AndFilter();
        List<FieldType> fieldTypes = andFilter.getFields();
        //非null过滤
        NotNullFilter notNullField = new NotNullFilter();
        notNullField.setDimension(dimension);
        fieldTypes.add(notNullField);

        //时间过滤
        PathAnalysisDto.BetweenField betweenField = new PathAnalysisDto.BetweenField();
        betweenField.setDimension(dimension);
        betweenField.setLower(historyStart);
        betweenField.setUpper(historyEnd);
        fieldTypes.add(betweenField);

        return new FilterAggregation(countAgg, andFilter);
    }

    @JsonProperty
    public String getHistoryEnd() {
        return historyEnd;
    }

    @JsonProperty
    public String getHistoryStart() {
        return historyStart;
    }

    @JsonProperty
    public List<Stage> getStages() {
        return stages;
    }
}

