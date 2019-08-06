package io.sugo.services.tag.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.services.usergroup.parser.Parser;

import java.util.Map;

/**
 * Created by chenyuzhi on 18-9-27.
 */
public class QueryUpdateBean {
	private final String hproxy;
	private final String dataSource;
	private final Parser parser;
	private final Map<String, Boolean> appendFlags;

	public QueryUpdateBean(
			@JsonProperty("hproxy") String hproxy,
			@JsonProperty("dataSource") String dataSource,
			@JsonProperty("parser") Parser parser,
			@JsonProperty("appendFlags") Map<String, Boolean> appendFlags) {
		this.hproxy = hproxy;
		this.dataSource = dataSource;
		this.parser = parser;
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

	@JsonProperty("parser")
	public Parser getParser() {
		return parser;
	}

	@JsonProperty("appendFlags")
	public Map<String, Boolean> getAppendFlags() {
		return appendFlags;
	}
}
