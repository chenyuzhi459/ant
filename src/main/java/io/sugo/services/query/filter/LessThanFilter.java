package io.sugo.services.query.filter;

public class LessThanFilter extends LessThanEqualFilter {
    Boolean upperStrict = true;

    public Boolean getUpperStrict() {
        return upperStrict;
    }

    public void setUpperStrict(Boolean upperStrict) {
        this.upperStrict = upperStrict;
    }
}