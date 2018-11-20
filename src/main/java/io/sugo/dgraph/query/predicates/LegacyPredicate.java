package io.sugo.dgraph.query.predicates;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.metamx.common.IAE;

import java.util.Map;

/**
 * Created by chenyuzhi on 18-11-19.
 */
public class LegacyPredicate extends DefaultPredicate {
	private static final String convertValue(Object predicate)
	{
		final String retVal;

		if (predicate instanceof String) {
			retVal = (String) predicate;
		} else {
			throw new IAE("Unknown type[%s] for predicate[%s]", predicate.getClass(), predicate);
		}

		return retVal;
	}

	@JsonCreator
	public LegacyPredicate(Object predicate) {
		super(convertValue(predicate), null, null, false);
	}
}
