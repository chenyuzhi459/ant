package io.sugo.services.query.filter;

public class BetweenEqualFilter extends BetweenFilter {
    Boolean lowerStrict = true;
    Boolean upperStrict = true;

    public Boolean getLowerStrict() {
        return lowerStrict;
    }

    public void setLowerStrict(Boolean lowerStrict) {
        this.lowerStrict = lowerStrict;
    }

    public Boolean getUpperStrict() {
        return upperStrict;
    }

    public void setUpperStrict(Boolean upperStrict) {
        this.upperStrict = upperStrict;
    }
}