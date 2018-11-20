package io.sugo.dgraph.query.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.dgraph.query.fiters.Filter;
import io.sugo.dgraph.query.pagination.Order;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chenyuzhi on 18-11-19.
 */
public class Facets {
	private final Filter filter;
	private final List<Order> orders;
	private final List<Facet> edges;

	@JsonCreator
	public Facets(
			@JsonProperty("filter") Filter filter,
			@JsonProperty("orders") List<Order> orders,
			@JsonProperty("edges") List<Facet> edges) {
		this.filter = filter;
		this.orders = orders == null ? Collections.emptyList() :orders;
		this.edges = edges == null ? Collections.emptyList() :edges;
	}

	@JsonProperty("filter")
	public Filter getFilter() {
		return filter;
	}

	@JsonProperty("orders")
	public List<Order> getOrders() {
		return orders;
	}

	@JsonProperty("edges")
	public List<Facet> getEdges() {
		return edges;
	}

	public String buildQueryString(){
		StringBuilder strBuilder = new StringBuilder();
		if(filter !=null ){
			strBuilder.append(String.format("@facets(%s) ", filter.buildQueryString()));
		}
		if(orders.size() > 0){
			List<String> orderStrs = orders.stream().map(Order::buildQueryString).collect(Collectors.toList());
			strBuilder.append(String.format("@facets(%s) ", String.join(",", orderStrs)));
		}

		if(edges.size() > 0){
			List<String> edgeStrs = edges.stream().map(Facet::buildQueryString).collect(Collectors.toList());
			strBuilder.append(String.format("@facets(%s) ", String.join(",", edgeStrs)));
		}else {
			strBuilder.append("@facets ");
		}

		return strBuilder.toString();
	}

}
