package io.sugo.services.query.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AndFilter  extends FieldType {
    String type = "and";
    List fields = new ArrayList<>();

    public AndFilter() {
        super.type = type;
    }

    @JsonProperty
    public List getFields() {
        return fields;
    }

    public void setFields(List fields) {
        this.fields = fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AndFilter)) return false;
        AndFilter andFilter = (AndFilter) o;
        return Objects.equals(type, andFilter.getType()) &&
                Objects.equals(fields, andFilter.getFields());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, fields);
    }
}