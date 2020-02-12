package io.sugo.services.query.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;

import java.util.Objects;

public class BetweenFilter extends BoundFilter {
    Object lower;
    Object upper;

    @JsonProperty
    public Object getLower() {
        return lower;
    }

    public void setLower(Object lower) {
        this.lower = lower;
    }

    @JsonProperty
    public Object getUpper() {
        return upper;
    }

    public void setUpper(Object upper) {
        this.upper = upper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BetweenFilter)) return false;
        BetweenFilter that = (BetweenFilter) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(getDimension(), that.getDimension()) &&
                Objects.equals(lower, that.getLower()) &&
                Objects.equals(upper, that.getUpper());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, getDimension(), lower, upper);
    }
}