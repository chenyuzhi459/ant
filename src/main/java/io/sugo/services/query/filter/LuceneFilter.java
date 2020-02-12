package io.sugo.services.query.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;

import java.util.Objects;

public class LuceneFilter extends FieldType {
    public LuceneFilter() {
        super.type = "lucene";
    }

    String query;

    @JsonProperty
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LuceneFilter)) return false;
        LuceneFilter that = (LuceneFilter) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(query, that.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, query);
    }
}
