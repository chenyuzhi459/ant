package io.sugo.dgraph.query.pagination;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by chenyuzhi on 18-11-19.
 */
public class Pagination {
	private final Integer first;
	private final Integer offset;
	private final String after;
	private final List<Order> orders;

	@JsonCreator
	public Pagination(
			@JsonProperty("first") Integer first,
			@JsonProperty("offset") Integer offset,
			@JsonProperty("after") String after,
			@JsonProperty("orders") List<Order> orders)
	{
		this.first = first == null ? -1 : first;
		this.offset = offset == null ? -1 : offset;
		this.after = after;
		this.orders = orders;
	}

	@JsonProperty("first")
	public int getFirst() {
		return first;
	}

	@JsonProperty("offset")
	public int getOffset() {
		return offset;
	}

	@JsonProperty("after")
	public String getAfter() {
		return after;
	}

	@JsonProperty("orders")
	public List<Order> getOrders() {
		return orders;
	}

	public String buildQueryString() {
//		StringBuilder strBuilder = new StringBuilder();
		List<String> paginationStr = new ArrayList<>();
		if(first >= 0){
			paginationStr.add(String.format("first: %s", first));
//			strBuilder.append("first: ").append(first);
		}
		if(offset >= 0 ){
			paginationStr.add(String.format("offset: %s", offset));
//			strBuilder.append(", offset: ").append(offset);
		}
		if(Strings.isNotBlank(after) ){
			paginationStr.add(String.format("after: %s", after));
//			strBuilder.append(", after: ").append(after);
		}
		if(Objects.nonNull(orders)){

			paginationStr.addAll(orders.stream().map(Order::buildQueryString).collect(Collectors.toList()));
//			strBuilder.append(String.join(", ", orderStrs));
		}

		return String.join(", ", paginationStr);
	}
}
