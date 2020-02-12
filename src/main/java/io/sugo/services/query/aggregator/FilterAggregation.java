package io.sugo.services.query.aggregator;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;
import io.sugo.services.query.filter.BetweenFilter;
import io.sugo.services.query.filter.FieldType;

import java.util.Objects;

public class FilterAggregation  extends Aggregation {
    String type = "lucene_filtered" ;
    Aggregation aggregator;
    //周期时间的过滤
   FieldType filter;

    public FilterAggregation(Aggregation aggregator,FieldType filter) {
        this.aggregator = aggregator;
        this.filter = filter;
    }

    @JsonProperty
    public String getType() {
        return type;
    }


    @JsonProperty
    public Aggregation getAggregator() {
        return aggregator;
    }

    public void setAggregator(Aggregation aggregator) {
        this.aggregator = aggregator;
    }

    @JsonProperty
    public FieldType getFilter() {
        return filter;
    }

    public void setFilter(BetweenFilter filter) {
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
