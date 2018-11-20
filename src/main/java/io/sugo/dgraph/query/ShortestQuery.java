package io.sugo.dgraph.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.utils.StringUtil;
import io.sugo.dgraph.query.predicates.Predicate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by chenyuzhi on 18-11-20.
 * user for:
 *<li> K-Shortest Path Queries <li/>
 */
public class ShortestQuery implements Query{
	private final String from;
	private final String to;
	private final Integer numpaths;
	private final String variableAs;
	private final List<Predicate> predicates;

	@JsonCreator
	public ShortestQuery(
			@JsonProperty("from") String from,
			@JsonProperty("to") String to,
			@JsonProperty("numpaths") Integer numpaths,
			@JsonProperty("variableAs") String variableAs,
			@JsonProperty("predicates") List<Predicate> predicates
	){
		this.from = from;
		this.to = to;
		this.numpaths = numpaths;
		this.variableAs = variableAs;
		this.predicates = predicates;
	}

	@JsonProperty("from")
	public String getFrom() {
		return from;
	}

	@JsonProperty("to")
	public String getTo() {
		return to;
	}

	@JsonProperty("numpaths")
	public Integer getNumpaths() {
		return numpaths;
	}

	@JsonProperty("variableAs")
	public String getVariableAs() {
		return variableAs;
	}

	@JsonProperty("predicates")
	public List<Predicate> getPredicates() {
		return predicates;
	}

	@Override
	public String toSQL() {
		int indentSize = 1;
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(StringUtil.getIndent(indentSize));

		sqlBuilder.append(variableAs).append(" as ");
		if(Objects.nonNull(numpaths)){
			sqlBuilder.append(String.format("shortest(from: %s, to: %s, numpaths: %s)", from, to, numpaths));
		}else {
			sqlBuilder.append(String.format("shortest(from: %s, to: %s)", from, to));
		}

		List<String> predicateStrs = predicates.stream()
				.map(predicate -> {return predicate.buildQueryString(indentSize + 1);})
				.collect(Collectors.toList());
		sqlBuilder.append("{\n");
		if(predicateStrs.size() > 0){
			predicateStrs.forEach((s) -> sqlBuilder.append(s).append("\n"));
		}
		sqlBuilder.append(StringUtil.getIndent(indentSize)).append("}\n");
		return sqlBuilder.toString();
	}
}