package io.sugo.services.tag.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by chenyuzhi on 18-9-27.
 */
public class QueryUpdateBean {
	private final String broker;
	private final Map<String, Object> query;
	private final String hproxy;
	private final String dataSource;
	private final Map<String, String> dimMap;
	private final Map<String, Boolean> appendFlags;

	public QueryUpdateBean(
			@JsonProperty("broker") String broker,
			@JsonProperty("query") Map<String, Object> query,
			@JsonProperty("hproxy") String hproxy,
			@JsonProperty("dataSource") String dataSource,
			@JsonProperty("dimMap") Map<String, String> dimMap,
			@JsonProperty("appendFlags") Map<String, Boolean> appendFlags) {
		this.broker = broker;
		this.query = query;
		this.hproxy = hproxy;
		this.dataSource = dataSource;
		this.dimMap = dimMap;
		this.appendFlags = appendFlags;
	}

	@JsonProperty("broker")
	public String getBroker() {
		return broker;
	}

	@JsonProperty("query")
	public Map<String, Object> getQuery() {
		return query;
	}

	@JsonProperty("hproxy")
	public String getHproxy() {
		return hproxy;
	}

	@JsonProperty("dataSource")
	public String getDataSource() {
		return dataSource;
	}

	@JsonProperty("dimMap")
	public Map<String, String> getDimMap() {
		return dimMap;
	}

	@JsonProperty("appendFlags")
	public Map<String, Boolean> getAppendFlags() {
		return appendFlags;
	}
}
