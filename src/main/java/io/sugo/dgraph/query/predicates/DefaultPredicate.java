package io.sugo.dgraph.query.predicates;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.utils.StringUtil;
import io.sugo.dgraph.query.Query;
import org.apache.logging.log4j.util.Strings;

/**
 * Created by chenyuzhi on 18-11-16.
 * Used for predicates:
 * <li>scalarPredicate <li/>
 * <li>_predicate_<li/>
 * <li>val(variable)<li/>
 */
public class DefaultPredicate implements Predicate {
	private final String name;
	private final String alias;
	private final String variableAs;
	private final boolean isExtract;

	@JsonCreator
	public DefaultPredicate(
			@JsonProperty("name") String name,
			@JsonProperty("alias") String alias,
			@JsonProperty("variableAs") String variableAs,
			@JsonProperty(value = "isExtract", defaultValue = "false") boolean isExtract)
	{
		this.name = name;
		this.alias = alias;
		this.variableAs = variableAs;
		this.isExtract = isExtract;
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

	@JsonProperty("isExtract")
	public boolean isExtract() {
		return isExtract;
	}

	@Override
	public String buildQueryString(int indentSize) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(StringUtil.getIndent(indentSize));
		if(Strings.isNotBlank(variableAs)){
			strBuilder.append(variableAs).append(" as ");
		}
		if(Strings.isNotBlank(alias)) {
			strBuilder.append(alias).append(": ");
		}
		if(isExtract){
			strBuilder.append(String.format("val(%s)", name));
		}else {
			strBuilder.append(name);
		}
		return strBuilder.toString();
	}
}
