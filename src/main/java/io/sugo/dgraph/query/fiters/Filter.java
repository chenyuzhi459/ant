package io.sugo.dgraph.query.fiters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by chenyuzhi on 18-11-16.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(value = {
		@JsonSubTypes.Type(name = "allofterms", value = AllOfTermsFilter.class),
		@JsonSubTypes.Type(name = "uid", value = UidFilter.class)
})
public interface Filter {
	String buildQueryString();
}
