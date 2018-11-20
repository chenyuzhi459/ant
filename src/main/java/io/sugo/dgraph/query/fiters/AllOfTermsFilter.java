package io.sugo.dgraph.query.fiters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by chenyuzhi on 18-11-16.
 */
public class AllOfTermsFilter implements Filter{

	private final String predicateName;
	private final List<String> values;

	@JsonCreator
	public AllOfTermsFilter(
			@JsonProperty("predicateName") String predicateName,
			@JsonProperty("values") List<String> values) {
		this.predicateName = predicateName;
		this.values = values;
	}

	@JsonProperty("predicateName")
	public String getPredicateName() {
		return predicateName;
	}

	@JsonProperty("values")
	public List<String> getValues() {
		return values;
	}

	@Override
	public String buildQueryString() {
		return String.format("allofterms(%s, \"%s\")", predicateName, String.join("," , values));

	}
}
