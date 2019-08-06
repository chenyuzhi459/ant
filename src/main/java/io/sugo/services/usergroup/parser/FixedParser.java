package io.sugo.services.usergroup.parser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import java.util.Map;

/**
 * Created by chenyuzhi on 19-8-5.
 * 输出一个固定的值
 */

public class FixedParser implements Parser{
	public static final String TYPE = "fixed";
	private String dimension;
	private Object value;

	@JsonCreator
	public FixedParser(
			@JsonProperty("name") String name,
			@JsonProperty("value") Object value) {
		Preconditions.checkNotNull(name, "dimension can not be null.");
		Preconditions.checkNotNull(value, "value can not be null.");
		this.dimension = name;
		this.value = value;
	}


	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getType() {
		return TYPE;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getDimension() {
		return dimension;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Object getValue() {
		return value;
	}

	public Object getParsedVal(Map<String,Object> data){
		return value;
	}
}
