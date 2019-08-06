package io.sugo.services.usergroup.parser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by chenyuzhi on 19-8-5.
 */
public class DefaultParser implements Parser {
	private static final String TYPE = "default";
	private Map<String, String> dimMap;


	@JsonCreator
	public DefaultParser(
		@JsonProperty("dimMap") Map<String, String> dimMap) {
		this.dimMap = dimMap;
	}

	@Override
	public Map<String, Object> parse(Map<String, Object> data, String groupByDim) {
		Map<String, Object> convertData = new HashMap<>();
		Iterator<Map.Entry<String, String>> dimMapIter = dimMap.entrySet().iterator();
		while (dimMapIter.hasNext()){
			Map.Entry<String, String> entry = dimMapIter.next();
			String queryDim = entry.getKey();
			String updateDim = entry.getValue();
			Object dataItem = data.get(queryDim);
			if(dataItem == null){
				continue;
			}
			convertData.put(updateDim, dataItem);
		}
		return convertData;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getType() {
		return null;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Map<String, String> getDimMap() {
		return dimMap;
	}
}
