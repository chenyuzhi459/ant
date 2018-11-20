package io.sugo.dgraph.query.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.utils.StringUtil;
import org.apache.logging.log4j.util.Strings;

/**
 * Created by chenyuzhi on 18-11-19.
 */
public class Facet {
	private final String name;
	private final String alias;
	private final String variableAs;

	@JsonCreator
	public Facet(
			@JsonProperty("name") String name,
			@JsonProperty("alias") String alias,
			@JsonProperty("variableAs") String variableAs) {
		this.name = name;
		this.alias = alias;
		this.variableAs = variableAs;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("alias")
	public String getAlias() {
		return alias;
	}

	@JsonProperty("variableAs")
	public String getVariableAs() {
		return variableAs;
	}

	String buildQueryString(){
		StringBuilder strBuilder = new StringBuilder();

		if(Strings.isNotBlank(variableAs)){
			strBuilder.append(variableAs).append(" as ");
		}
		if(Strings.isNotBlank(alias)) {
			strBuilder.append(alias).append(": ");
		}
		strBuilder.append(name);
		return strBuilder.toString();
	}
}
