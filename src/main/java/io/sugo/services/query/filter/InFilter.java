package io.sugo.services.query.filter;

import java.util.ArrayList;
import java.util.List;

public class InFilter extends Filter {
    List values = new ArrayList<>();

    public InFilter() {
        super.type = "in";
    }

    public List getValues() {
        return values;
    }

    public void setValues(List values) {
        this.values = values;
    }
}
