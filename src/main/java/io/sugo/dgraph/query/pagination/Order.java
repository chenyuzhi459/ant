package io.sugo.dgraph.query.pagination;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by chenyuzhi on 18-11-19.
 */
public class Order {
	private static final String ASC_DIRECTION = "asc";
	private static final String DESC_DIRECTION = "desc";
	private final String direction;
	private final String predicateName;
	private final Boolean isExtract;

	@JsonCreator
	public Order(
			@JsonProperty(value = "direction", defaultValue = "asc") String direction,
			@JsonProperty("predicateName") String predicateName,
			@JsonProperty(value = "isExtract",defaultValue = "false") Boolean isExtract)
	{
		this.direction = direction;
		this.predicateName = predicateName;
		this.isExtract = isExtract;
	}


	@JsonProperty("direction")
	public String getDirection() {
		return direction;
	}

	@JsonProperty("predicateName")
	public String getPredicateName() {
		return predicateName;
	}

	@JsonProperty("isExtract")
	public Boolean isExtract() {
		return isExtract;
	}

	public String buildQueryString() {
		StringBuilder strBuilder = new StringBuilder();
		if(direction.toLowerCase().equals(DESC_DIRECTION)){
			strBuilder.append("orderdesc");
		}else {
			strBuilder.append("orderasc");
		}
		strBuilder.append(": ");
		if(isExtract){
			strBuilder.append(String.format("val(%s)", predicateName));
		}else {
			strBuilder.append(predicateName);
		}
		return strBuilder.toString();
	}
}
