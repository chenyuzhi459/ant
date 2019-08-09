package io.sugo.services.usergroup.parser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import java.util.Map;

/**
 * Created by chenyuzhi on 19-8-5.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class MappingParser implements Parser {
	public static final String TYPE = "mapping";
	private String name;
	private String mapName;

	@JsonCreator
	public MappingParser(
		@JsonProperty("name") String name,   //uindex 维度名
		@JsonProperty("mapName") String mapName) {
		Preconditions.checkNotNull(name, "name can not be null.");
		Preconditions.checkNotNull(mapName, "mapName can not be null.");
		this.name = name;
		this.mapName = mapName;
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
	public String getMapName() {
		return mapName;
	}

	@Override
	public Object getParsedVal(Map<String, Object> data) {
		return data.get(mapName);
	}
}
