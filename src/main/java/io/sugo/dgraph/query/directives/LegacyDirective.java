package io.sugo.dgraph.query.directives;

import com.metamx.common.IAE;
import io.sugo.dgraph.query.DefaultQuery;

/**
 * Created by chenyuzhi on 18-11-19.
 */
public class LegacyDirective extends DefaultDirective {
	private static final String convertValue(Object directive)
	{
		final String retVal;

		if (directive instanceof String) {
			retVal = (String) directive;
		} else {
			throw new IAE("Unknown type[%s] for directive[%s]", directive.getClass(), directive);
		}

		return retVal;
	}

	public LegacyDirective(String directive) {
		super(convertValue(directive));
	}
}
