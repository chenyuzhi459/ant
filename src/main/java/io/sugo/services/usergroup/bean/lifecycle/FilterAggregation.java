package io.sugo.services.usergroup.bean.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.utils.RFMUtil;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;

import java.util.Objects;

public class FilterAggregation  extends RFMUtil.Aggregation {
    String type = "lucene_filtered" ;
    RFMUtil.Aggregation aggregator;
    //周期时间的过滤
    PathAnalysisDto.FieldType filter;

    public FilterAggregation(RFMUtil.Aggregation aggregator, PathAnalysisDto.FieldType filter) {
        this.aggregator = aggregator;
        this.filter = filter;
    }

    @JsonProperty
    public String getType() {
        return type;
    }


    @JsonProperty
    public RFMUtil.Aggregation getAggregator() {
        return aggregator;
    }

    public void setAggregator(RFMUtil.Aggregation aggregator) {
        this.aggregator = aggregator;
    }

    @JsonProperty
    public PathAnalysisDto.FieldType getFilter() {
        return filter;
    }

    public void setFilter(PathAnalysisDto.BetweenField filter) {
        this.filter = filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterAggregation)) return false;
        FilterAggregation that = (FilterAggregation) o;
        return Objects.equals(getType(), that.getType()) &&
                Objects.equals(getAggregator(), that.getAggregator()) &&
                Objects.equals(getFilter(), that.getFilter());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getAggregator(), getFilter());
    }
}
