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
@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class FixedParser implements Parser{
	public static final String TYPE = "fixed";
	private String name;
	private Object value;

	@JsonCreator
	public FixedParser(
			@JsonProperty("name") String name, //uindex 维度名
			@JsonProperty("value") Object value) {
		Preconditions.checkNotNull(name, "name can not be null.");
		Preconditions.checkNotNull(value, "value can not be null.");
		this.name = name;
		this.value = value;
	}


	@JsonProperty
	public String getType() {
		return TYPE;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	@JsonProperty
	public Object getValue() {
		return value;
	}

	public Object getParsedVal(Map<String,Object> data){
		return value;
	}
}
