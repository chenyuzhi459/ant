package io.sugo.dgraph.query.directives;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by chenyuzhi on 18-11-19.
 */
public class GroupByDirective implements Directive {
	private final String predicateName;

	@JsonCreator
	public GroupByDirective(
			@JsonProperty("predicateName") String predicateName)
	{
		this.predicateName = predicateName;
	}

	@JsonProperty("predicateName")
	public String getPredicateName() {
		return predicateName;
	}

	@Override
	public String buildQueryString() {
		return String.format("@groupby(%s)", predicateName);
	}
}
