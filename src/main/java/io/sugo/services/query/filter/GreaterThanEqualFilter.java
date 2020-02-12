package io.sugo.services.query.filter;

public class GreaterThanEqualFilter extends BoundFilter {
    String lower;

    public String getLower() {
        return lower;
    }

    public void setLower(String lower) {
        this.lower = lower;
    }
}