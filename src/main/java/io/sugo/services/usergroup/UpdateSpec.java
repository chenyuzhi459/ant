package io.sugo.services.usergroup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.services.usergroup.parser.Parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyuzhi on 18-9-27.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class UpdateSpec {
	private final String hproxy;
	private final String dataSource;
	private final String uindexKey;
	private final List<Parser> parsers;
	private final Map<String, Boolean> appendFlags;

	@JsonCreator
	public UpdateSpec(
			@JsonProperty("hproxy") String hproxy,
			@JsonProperty("dataSource") String dataSource,
			@JsonProperty("uindexKey") String uindexKey,
			@JsonProperty("parsers") List<Parser> parsers,
			@JsonProperty("appendFlags") Map<String, Boolean> appendFlags) {
		this.hproxy = hproxy;
		this.dataSource = dataSource;
		this.uindexKey = uindexKey;
		this.parsers = parsers;
		this.appendFlags = appendFlags;
	}


	@JsonProperty("hproxy")
	public String getHproxy() {
		return hproxy;
	}

	@JsonProperty("dataSource")
	public String getDataSource() {
		return dataSource;
	}

	@JsonProperty("uindexKey")
	public String getUindexKey() {
		return uindexKey;
	}

	@JsonProperty("parsers")
	public List<Parser> getParsers() {
		return parsers;
	}

	@JsonProperty("appendFlags")
	public Map<String, Boolean> getAppendFlags() {
		return appendFlags;
	}

	public Map<String, Object> convert(Map<String, Object> data){
		Map<String,Object> convertData = new HashMap<>(parsers.size());
		for(Parser parser: parsers){
			convertData.put(parser.getName(), parser.getParsedVal(data));
		}
		return convertData;
	}


}
