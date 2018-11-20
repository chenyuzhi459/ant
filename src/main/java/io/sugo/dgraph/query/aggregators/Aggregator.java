package io.sugo.dgraph.query.aggregators;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by chenyuzhi on 18-11-19.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = AggPredicate.class)
@JsonSubTypes(value = {
		@JsonSubTypes.Type(name = "default", value = AggPredicate.class)
})
public interface Aggregator {
	String buildQueryString(int indentSize);
}
