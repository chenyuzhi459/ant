package io.sugo.dgraph.query.directives;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by chenyuzhi on 18-11-16.
 */
public class DefaultDirective implements Directive {
	private final String name;

	@JsonCreator
	public DefaultDirective(
			@JsonProperty("name") String name)
	{
		this.name = name;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@Override
	public String buildQueryString() {
		return String.format("@%s", name);
	}
}
