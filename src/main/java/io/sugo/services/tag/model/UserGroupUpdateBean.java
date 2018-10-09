package io.sugo.services.tag.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.redis.RedisDataIOFetcher;

import java.util.Map;

/**
 * Created by chenyuzhi on 18-9-25.
 */
public class UserGroupUpdateBean {
	private final String hproxy;
	private final String dataSource;
	private final String primaryColumn;
	private final RedisDataIOFetcher userGroupConfig;
	private final Map<String, Object> dimData;
	private final Map<String, Boolean> appendFlags;

	public UserGroupUpdateBean(
			@JsonProperty("hproxy") String hproxy,
			@JsonProperty("dataSource") String dataSource,
			@JsonProperty("primaryColumn") String primaryColumn,
			@JsonProperty("userGroupConfig") RedisDataIOFetcher userGroupConfig,
			@JsonProperty("dimData") Map<String, Object> dimData,
			@JsonProperty("appendFlags") Map<String, Boolean> appendFlags) {
		this.hproxy = hproxy;
		this.dataSource = dataSource;
		this.primaryColumn = primaryColumn;
		this.userGroupConfig = userGroupConfig;
		this.dimData = dimData;
		this.appendFlags = appendFlags;
	}

	@JsonProperty
	public String getHproxy() {
		return hproxy;
	}

	@JsonProperty
	public String getDataSource() {
		return dataSource;
	}

	@JsonProperty
	public String getPrimaryColumn() {
		return primaryColumn;
	}

	@JsonProperty
	public RedisDataIOFetcher getUserGroupConfig() {
		return userGroupConfig;
	}

	@JsonProperty
	public Map<String, Object> getDimData() {
		return dimData;
	}

	@JsonProperty
	public Map<String, Boolean> getAppendFlags() {
		return appendFlags;
	}
}
