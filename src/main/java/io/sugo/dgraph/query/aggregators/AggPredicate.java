package io.sugo.dgraph.query.aggregators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.utils.StringUtil;
import io.sugo.dgraph.query.predicates.Predicate;
import org.apache.logging.log4j.util.Strings;

/**
 * Created by chenyuzhi on 18-11-19.
 * Used for aggregators:
 * <li>count <li/>
 * <li>max<li/>
 * <li>min<li/>
 * <li>sum<li/>
 */
public class AggPredicate implements Predicate {
	private final String aggregatorName;
	private final String predicateName;
	private final String alias;
	private final boolean isExtract;
	private final String variableAs;

	@JsonCreator
	public AggPredicate(
			@JsonProperty("aggregatorName") String aggregatorName,
			@JsonProperty("predicateName") String predicateName,
			@JsonProperty("alias") String alias,
			@JsonProperty(value = "isExtract", defaultValue = "false") boolean isExtract,
			@JsonProperty("variableAs") String variableAs) {
		this.aggregatorName = aggregatorName;
		this.predicateName = predicateName;
		this.alias = alias;
		this.isExtract = isExtract;
		this.variableAs = variableAs;
	}

	@JsonProperty("aggregatorName")
	public String getAggregatorName() {
		return aggregatorName;
	}

	@JsonProperty("predicateName")
	public String getPredicateName() {
		return predicateName;
	}

	@JsonProperty("alias")
	public String getAlias() {
		return alias;
	}

	@JsonProperty("isExtract")
	public boolean isExtract() {
		return isExtract;
	}

	@JsonProperty("variableAs")
	public String getVariableAs() {
		return variableAs;
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
		strBuilder.append(aggregatorName);
		if(isExtract){
			strBuilder.append(String.format("(val(%s))", predicateName));
		}else {
			strBuilder.append(String.format("(%s)", predicateName));
		}
		return strBuilder.toString();
	}
}
