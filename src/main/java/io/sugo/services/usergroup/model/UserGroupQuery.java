package io.sugo.services.usergroup.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.redis.RedisDataIOFetcher;

import java.util.List;
import java.util.Map;

public class UserGroupQuery {
	String queryType = "user_group";
	String dataSource;
	Object intervals;
	Map filter;
	String granularity;
	String dimension;
	RedisDataIOFetcher dataConfig;
	List aggregations;
	List postAggregations;
	Map having;
	Map context;

	public UserGroupQuery() {
	}

	public UserGroupQuery(
			@JsonProperty("dataSource") String dataSource,
			@JsonProperty("intervals") Object intervals,
			@JsonProperty("filter") Map filter,
			@JsonProperty("granularity") String granularity,
			@JsonProperty("dimension") String dimension,
			@JsonProperty("dataConfig") RedisDataIOFetcher dataConfig,
			@JsonProperty("aggregations") List aggregations,
			@JsonProperty("postAggregations") List postAggregations,
			@JsonProperty("having") Map having,
			@JsonProperty("context") Map<String, Object> context
	) {
		this.dataSource = dataSource;
		this.intervals = intervals;
		this.filter = filter;
		this.granularity = granularity;
		this.dimension = dimension;
		this.dataConfig = dataConfig;
		this.aggregations = aggregations;
		this.postAggregations = postAggregations;
		this.having = having;
		this.context = context;
	}

	@JsonProperty("queryType")
	public String getQueryType() {
		return queryType;
	}

	public UserGroupQuery setQueryType(String queryType) {
		this.queryType = queryType;
		return this;
	}

	@JsonProperty("dataSource")
	public String getDataSource() {
		return dataSource;
	}

	public UserGroupQuery setDataSource(String dataSource) {
		this.dataSource = dataSource;
		return this;
	}

	@JsonProperty("intervals")
	public Object getIntervals() {
		return intervals;
	}

	public UserGroupQuery setIntervals(Object intervals) {
		this.intervals = intervals;
		return this;
	}

	@JsonProperty("filter")
	public Map getFilter() {
		return filter;
	}

	public UserGroupQuery setFilter(Map filter) {
		this.filter = filter;
		return this;
	}

	@JsonProperty("dimension")
	public String getDimension() {
		return dimension;
	}

	public UserGroupQuery setDimension(String dimension) {
		this.dimension = dimension;
		return this;
	}

	@JsonProperty("granularity")
	public String getGranularity() {
		return granularity;
	}

	public UserGroupQuery setGranularity(String granularity) {
		this.granularity = granularity;
		return this;
	}

	@JsonProperty("aggregations")
	public List getAggregations() {
		return aggregations;
	}

	public UserGroupQuery setAggregations(List aggregations) {
		this.aggregations = aggregations;
		return this;
	}

	@JsonProperty("postAggregations")
	public List getPostAggregations() {
		return postAggregations;
	}

	public UserGroupQuery setPostAggregations(List postAggregations) {
		this.postAggregations = postAggregations;
		return this;
	}

	@JsonProperty("having")
	public Map getHaving() {
		return having;
	}

	public UserGroupQuery setHaving(Map having) {
		this.having = having;
		return this;
	}

	@JsonProperty("context")
	public Map getContext() {
		return context;
	}

	@JsonProperty("dataConfig")
	public RedisDataIOFetcher getDataConfig() {
		return dataConfig;
	}

	public UserGroupQuery setDataConfig(RedisDataIOFetcher dataConfig) {
		this.dataConfig = dataConfig;
		return this;
	}

	public UserGroupQuery setContext(Map context) {
		this.context = context;
		return this;
	}
}
