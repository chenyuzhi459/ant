package io.sugo.services.query.filter;

public class NotInFilter extends FieldType {
    public NotInFilter() {
        super.type = "not";
        field = new InFilter();
    }

    InFilter field;

    public InFilter getField() {
        return field;
    }

    public void setField(InFilter field) {
        this.field = field;
    }
}

