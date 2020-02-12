package io.sugo.services.query.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;

import java.util.Objects;

public class NotNullFilter extends FieldType {
    public NotNullFilter() {
        super.type = "not";
        field = new LuceneFilter();
    }

    LuceneFilter field;

    @JsonProperty
    public LuceneFilter getField() {
        return field;
    }

    public void setField(LuceneFilter field) {
        this.field = field;
    }

    public void setDimension(String dimension){
        this.field.setQuery(String.format("(*:* NOT %s:*)", dimension));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotNullFilter)) return false;
        NotNullFilter that = (NotNullFilter) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(field, that.getField());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, field);
    }
}
