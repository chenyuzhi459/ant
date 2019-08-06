package io.sugo.services.usergroup.model.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.services.usergroup.model.query.Query;

import java.util.List;
import java.util.Map;

/**
 * Created by chenyuzhi on 19-8-5.
 */
public class GroupByQuery implements Query {
	String queryType = "lucene_groupBy";
	String dataSource;
	Object intervals;
	Map filter;
	String granularity;
	List dimensions;
	List aggregations;
	List postAggregations;
	Map having;
	Map limitSpec;
	Map context;

	public GroupByQuery(
			@JsonProperty("dataSource") String dataSource,
			@JsonProperty("intervals") Object intervals,
			@JsonProperty("filter") Map filter,
			@JsonProperty("granularity") String granularity,
			@JsonProperty("dimensions") List dimensions,
			@JsonProperty("aggregations") List aggregations,
			@JsonProperty("postAggregations") List postAggregations,
			@JsonProperty("having") Map having,
			@JsonProperty("limitSpec") Map limitSpec,
			@JsonProperty("context") Map<String, Object> context
	) {
		this.dataSource = dataSource;
		this.intervals = intervals;
		this.filter = filter;
		this.granularity = granularity;
		this.dimensions = dimensions;
		this.aggregations = aggregations;
		this.postAggregations = postAggregations;
		this.having = having;
		this.context = context;
		this.limitSpec = limitSpec;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getDataSource() {
		return dataSource;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Object getIntervals() {
		return intervals;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Map getFilter() {
		return filter;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getGranularity() {
		return granularity;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List getDimensions() {
		return dimensions;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List getAggregations() {
		return aggregations;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List getPostAggregations() {
		return postAggregations;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Map getHaving() {
		return having;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Map getLimitSpec() {
		return limitSpec;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Map getContext() {
		return context;
	}

	@Override
	public RedisDataIOFetcher getDataConfig() {
		throw new UnsupportedOperationException("Not support to getDataConfig for groupBy query");
	}


}
