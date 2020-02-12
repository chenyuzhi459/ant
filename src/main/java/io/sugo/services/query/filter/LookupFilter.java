package io.sugo.services.query.filter;

public class LookupFilter extends Filter {
    public LookupFilter() {
        super.type = "lookup";
    }

    String lookup;

    public String getLookup() {
        return lookup;
    }

    public void setLookup(String lookup) {
        this.lookup = lookup;
    }
}