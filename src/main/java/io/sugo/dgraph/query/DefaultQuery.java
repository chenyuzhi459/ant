package io.sugo.dgraph.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.utils.StringUtil;
import io.sugo.dgraph.query.directives.Directive;
import io.sugo.dgraph.query.fiters.Filter;
import io.sugo.dgraph.query.pagination.Pagination;
import io.sugo.dgraph.query.predicates.Predicate;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by chenyuzhi on 18-11-16.
 */
public class DefaultQuery implements Query {
	private final String queryName;
	private final String variableAs;
	private final Filter func;
	private final Filter filter;
	private final Directive directive;
	private final Pagination pagination;
	private final List<Predicate> predicates;

	@JsonCreator
	public DefaultQuery(
			@JsonProperty("queryName") String queryName,
			@JsonProperty("variableAs") String variableAs,
			@JsonProperty("func") Filter func,
			@JsonProperty("filter") Filter filter,
			@JsonProperty("directive") Directive directive,
			@JsonProperty("pagination") Pagination pagination,
			@JsonProperty("predicates") List<Predicate> predicates)
	{
		this.queryName = queryName;
		this.variableAs = variableAs;
		this.func = func;
		this.filter = filter;
		this.directive = directive;
		this.pagination = pagination;
		this.predicates = predicates == null ? Collections.emptyList() : predicates;
	}

	@JsonProperty("queryName")
	public String getQueryName() {
		return queryName;
	}

	@JsonProperty("variableAs")
	public String getVariableAs() {
		return variableAs;
	}

	@JsonProperty("func")
	public Filter getFunc() {
		return func;
	}

	@JsonProperty("filter")
	public Filter getFilter() {
		return filter;
	}

	@JsonProperty("directive")
	public Directive getDirective() {
		return directive;
	}

	@JsonProperty("pagination")
	public Pagination getPagination() {
		return pagination;
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
		if(Strings.isNotBlank(variableAs)){
			sqlBuilder.append(variableAs).append(" as ");
		}
		sqlBuilder.append(queryName).append("(");
		sqlBuilder.append("func: ").append(func.buildQueryString());
		if(Objects.nonNull(pagination)){
			sqlBuilder.append(", ").append(pagination.buildQueryString());
		}
		sqlBuilder.append(")");
		if(Objects.nonNull(filter)){
			sqlBuilder.append(" @filter(").append(filter.buildQueryString()).append(")");
		}
		if(Objects.nonNull(directive)){
			sqlBuilder.append(" ").append(directive.buildQueryString());
		}

		List<String> predicateStrs = new ArrayList<>();

		predicateStrs.addAll(predicates.stream()
				.map(predicate -> {return predicate.buildQueryString(indentSize + 1);})
				.collect(Collectors.toList()));

		sqlBuilder.append("{\n");
		if(predicateStrs.size() > 0){
			predicateStrs.forEach((s) -> sqlBuilder.append(s).append("\n"));
		}
		sqlBuilder.append(StringUtil.getIndent(indentSize)).append("}\n");


		return sqlBuilder.toString();
	}

}
