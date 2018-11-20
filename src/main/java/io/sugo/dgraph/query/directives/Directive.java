package io.sugo.dgraph.query.directives;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by chenyuzhi on 18-11-16.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = LegacyDirective.class)
@JsonSubTypes(value = {
		@JsonSubTypes.Type(name = "default", value = DefaultDirective.class),
		@JsonSubTypes.Type(name = "recurse", value = RecurseDirective.class),
		@JsonSubTypes.Type(name = "groupby", value = GroupByDirective.class)
})
public interface Directive {
	String buildQueryString();
}
