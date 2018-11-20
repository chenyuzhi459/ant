package io.sugo.dgraph.query;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.sugo.dgraph.query.predicates.DefaultPredicate;
import io.sugo.dgraph.query.predicates.UidPredicate;

/**
 * Created by chenyuzhi on 18-11-16.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "queryType", defaultImpl = DefaultQuery.class)
@JsonSubTypes(value = {
		@JsonSubTypes.Type(name = "default", value = DefaultQuery.class),
		@JsonSubTypes.Type(name = "shortest", value = ShortestQuery.class)
})
public interface Query {//TODO add shortestQuery and schemaQuery
	String toSQL();
}
