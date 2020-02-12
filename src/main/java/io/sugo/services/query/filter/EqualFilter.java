package io.sugo.services.query.filter;

public class EqualFilter extends Filter {
    public EqualFilter() {
        super.type = "selector";
    }

    String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
