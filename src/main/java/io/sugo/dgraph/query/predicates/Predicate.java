package io.sugo.dgraph.query.predicates;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.sugo.dgraph.query.aggregators.AggPredicate;
import io.sugo.dgraph.query.math.MathPredicate;

/**
 * Created by chenyuzhi on 18-11-16.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = LegacyPredicate.class)
@JsonSubTypes(value = {
		@JsonSubTypes.Type(name = "default", value = DefaultPredicate.class),
		@JsonSubTypes.Type(name = "uid", value = UidPredicate.class),
		@JsonSubTypes.Type(name = "math", value = MathPredicate.class),
		@JsonSubTypes.Type(name = "aggregtor", value = AggPredicate.class),
})
public interface Predicate {//TODO add expand-predicate
	String buildQueryString(int indentSize);
}
