package io.sugo.services.tag.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by chenyuzhi on 18-9-27.
 */
public class QueryUpdateBean {
	private final String brokerUrl;
	private final Map<String, Object> query;
	private final String hproxyUrl;
	private final String dataSource;
	private final Map<String, String> dimMap;
	private final Map<String, Boolean> appendFlags;

	public QueryUpdateBean(
			@JsonProperty("brokerUrl") String brokerUrl,
			@JsonProperty("query") Map<String, Object> query,
			@JsonProperty("hproxyUrl") String hproxyUrl,
			@JsonProperty("dataSource") String dataSource,
			@JsonProperty("dimMap") Map<String, String> dimMap,
			@JsonProperty("appendFlags") Map<String, Boolean> appendFlags) {
		this.brokerUrl = brokerUrl;
		this.query = query;
		this.hproxyUrl = hproxyUrl;
		this.dataSource = dataSource;
		this.dimMap = dimMap;
		this.appendFlags = appendFlags;
	}

	@JsonProperty("brokerUrl")
	public String getBrokerUrl() {
		return brokerUrl;
	}

	@JsonProperty("query")
	public Map<String, Object> getQuery() {
		return query;
	}

	@JsonProperty("hproxyUrl")
	public String getHproxyUrl() {
		return hproxyUrl;
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
