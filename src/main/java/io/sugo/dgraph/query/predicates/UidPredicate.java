package io.sugo.dgraph.query.predicates;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.utils.StringUtil;
import io.sugo.dgraph.query.aggregators.Aggregator;
import io.sugo.dgraph.query.facet.Facets;
import io.sugo.dgraph.query.fiters.Filter;
import io.sugo.dgraph.query.math.MathPredicate;
import io.sugo.dgraph.query.pagination.Pagination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chenyuzhi on 18-11-16.
 */
public class UidPredicate extends DefaultPredicate {
	private final Filter filter;
	private final Pagination pagination;
	private final Facets facets;
	private final List<Predicate> predicates;

	public UidPredicate(
			@JsonProperty("name") String name,
			@JsonProperty("alias") String alias,
			@JsonProperty("variableAs") String variableAs,
			@JsonProperty("filter") Filter filter,
			@JsonProperty("pagination") Pagination pagination,
			@JsonProperty("facets") Facets facets,
			@JsonProperty("predicates") List<Predicate> predicates)
	{
		super(name, alias, variableAs, false);
		this.filter = filter;
		this.pagination = pagination;
		this.facets = facets;
		this.predicates = predicates == null ? Collections.emptyList() : predicates;
	}

	@JsonProperty("filter")
	public Filter getFilter() {
		return filter;
	}

	@JsonProperty("pagination")
	public Pagination getPagination() {
		return pagination;
	}

	@JsonProperty("maths")
	public Facets getFacets() {
		return facets;
	}

	@JsonProperty("predicates")
	public List<Predicate> getPredicates() {
		return predicates;
	}

	@Override
	public String buildQueryString(int indentSize) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(super.buildQueryString(indentSize));
		if(filter != null){
			strBuilder.append(String.format(" @filter(%s)", filter.toString()));
		}

		if(pagination != null){
			strBuilder.append(String.format(" (%s)", pagination.buildQueryString()));
		}

		if(facets != null){
			strBuilder.append(" ").append(facets.buildQueryString());
		}

		List<String> predicateStrs = new ArrayList<>();

		predicateStrs.addAll(predicates.stream()
				.map(predicate -> {return predicate.buildQueryString(indentSize + 1);})
				.collect(Collectors.toList()));

		if(predicateStrs.size() > 0){
			strBuilder.append("{\n");
			predicateStrs.forEach((s)-> strBuilder.append(s).append("\n"));
			strBuilder.append(StringUtil.getIndent(indentSize)).append("}");
		}

		return strBuilder.toString();
	}
}
