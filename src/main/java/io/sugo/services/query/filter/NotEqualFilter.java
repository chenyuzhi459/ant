package io.sugo.services.query.filter;

public class NotEqualFilter extends FieldType {
    public NotEqualFilter() {
        super.type = "not";
        field = new EqualFilter();
    }

    EqualFilter field;

    public EqualFilter getField() {
        return field;
    }

    public void setField(EqualFilter field) {
        this.field = field;
    }
}
