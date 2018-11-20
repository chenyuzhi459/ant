package io.sugo.dgraph.query.math;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.utils.StringUtil;
import io.sugo.dgraph.query.predicates.Predicate;
import org.apache.logging.log4j.util.Strings;

/**
 * Created by chenyuzhi on 18-11-19.
 */
public class MathPredicate implements Predicate{
	private final String expression;
	private final String variableAs;

	@JsonCreator
	public MathPredicate(
			@JsonProperty("expression") String expression,
			@JsonProperty("variableAs") String variableAs) {
		this.expression = expression;
		this.variableAs = variableAs;
	}

	@JsonProperty("expression")
	public String getExpression() {
		return expression;
	}

	@JsonProperty("variableAs")
	public String getVariableAs() {
		return variableAs;
	}

	public String buildQueryString(int indentSize){
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(StringUtil.getIndent(indentSize));
		if(Strings.isNotBlank(variableAs)){
			strBuilder.append(variableAs).append(" as ");
		}
		strBuilder.append(String.format("math(%s)", expression));
		return strBuilder.toString();
	}
}
