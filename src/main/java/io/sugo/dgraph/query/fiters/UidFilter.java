package io.sugo.dgraph.query.fiters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by chenyuzhi on 18-11-19.
 */
public class UidFilter implements Filter{
	private final List<String> values;

	@JsonCreator
	public UidFilter(
			@JsonProperty("values") List<String> values)
	{
		this.values = values;
	}

	@JsonProperty("values")
	public List<String> getValues() {
		return values;
	}

	@Override
	public String buildQueryString() {

		return String.format("uid(%s)", String.join(",", values));
	}
}
