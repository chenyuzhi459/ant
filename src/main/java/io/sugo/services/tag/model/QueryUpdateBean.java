package io.sugo.services.tag.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.services.usergroup.parser.Parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenyuzhi on 18-9-27.
 */
public class QueryUpdateBean {
	private final String hproxy;
	private final String dataSource;
	private final List<Parser> parsers;
	private final Map<String, Boolean> appendFlags;

	public QueryUpdateBean(
			@JsonProperty("hproxy") String hproxy,
			@JsonProperty("dataSource") String dataSource,
			@JsonProperty("parsers") List<Parser> parsers,
			@JsonProperty("appendFlags") Map<String, Boolean> appendFlags) {
		this.hproxy = hproxy;
		this.dataSource = dataSource;
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
			convertData.put(parser.getDimension(), parser.getParsedVal(data));
		}
		return convertData;
	}


}
