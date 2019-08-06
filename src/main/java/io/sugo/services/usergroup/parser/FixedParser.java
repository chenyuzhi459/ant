package io.sugo.services.usergroup.parser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenyuzhi on 19-8-5.
 * 输出一个固定的值
 */

public class FixedParser implements Parser{
	private static final String TYPE = "fixed";
	private final Map<String, Object> dimData;
	private final String newPrimaryCol;

	@JsonCreator
	public FixedParser(
			@JsonProperty("dimData") Map<String, Object> dimData,
			@JsonProperty("u_primaryCol") String newPrimaryCol) {
		this.dimData = dimData;
		this.newPrimaryCol = newPrimaryCol;
	}

	@Override
	public Map<String, Object> parse(Map<String, Object> data, String groupByDim) {
		Map<String, Object> convertData = new HashMap<>();
		convertData.put(newPrimaryCol, data.get(groupByDim));
		convertData.putAll(dimData);
		return convertData;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getType() {
		return null;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Map<String, Object> getDimData() {
		return dimData;
	}


	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getNewPrimaryCol() {
		return newPrimaryCol;
	}
}
