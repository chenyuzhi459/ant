package io.sugo.services.query.filter;

public class GreaterThanFilter extends GreaterThanEqualFilter {
    Boolean lowerStrict = true;

    public Boolean getLowerStrict() {
        return lowerStrict;
    }

    public void setLowerStrict(Boolean lowerStrict) {
        this.lowerStrict = lowerStrict;
    }
}
