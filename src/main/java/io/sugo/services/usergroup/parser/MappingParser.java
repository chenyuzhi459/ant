package io.sugo.services.usergroup.parser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import java.util.Map;

/**
 * Created by chenyuzhi on 19-8-5.
 */
public class MappingParser implements Parser {
	public static final String TYPE = "mapping";
	private String dimension;
	private String mapName;

	@JsonCreator
	public MappingParser(
		@JsonProperty("name") String dimension,
		@JsonProperty("mapName") String mapName) {
		Preconditions.checkNotNull(dimension, "dimension can not be null.");
		Preconditions.checkNotNull(mapName, "mapName can not be null.");
		this.dimension = dimension;
		this.mapName = mapName;
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
	public String getMapName() {
		return mapName;
	}

	@Override
	public Object getParsedVal(Map<String, Object> data) {
		return data.get(mapName);
	}
}
