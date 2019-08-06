package io.sugo.services.usergroup.model;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.sugo.common.redis.RedisInfo;
import io.sugo.common.utils.QueryUtil;
import io.sugo.services.tag.DataUpdateHelper;
import io.sugo.services.tag.model.QueryUpdateBean;
import io.sugo.services.usergroup.model.query.GroupByQuery;
import io.sugo.services.usergroup.model.query.Query;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenyuzhi on 19-8-5.
 */
public class TindexGroupBean extends UserGroupBean {
	private static final String TYPE = "tindex";
	protected QueryUpdateBean to;   //用于说明把计算结果发往哪个地方
	protected  String broker;
	private DataUpdateHelper dataUpdateHelper;
	private String groupByDim;

	@JsonCreator
	public TindexGroupBean(
			@JsonProperty("type") String type,
			@JsonProperty("broker") String broker,
			@JsonProperty("query") Query query,
			@JsonProperty("op") String op,
			@JsonProperty("groupByDim") String groupByDim,
			@JsonProperty("to") QueryUpdateBean to,
			@JacksonInject DataUpdateHelper dataUpdateHelper

	) {
		super(type,query, op);
		Preconditions.checkNotNull(broker, "broker can not be null.");
		Preconditions.checkNotNull(groupByDim, "groupByDim can not be null.");
		this.broker = broker;
		this.dataUpdateHelper = dataUpdateHelper;
		this.groupByDim = groupByDim;
		this.to = to;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getType() {
		return TYPE;
	}

	@Override
	public  Set<String>  getData(Map<RedisInfo, Set<String>> tempGroups) {
		//TODO 后续考虑兼容旧接口临时分群的处理
		List<Map> queryResult = QueryUtil.getGroupByQueryResult(getBroker(),(GroupByQuery)query);
		if(to != null){
			this.dataUpdateHelper.updateQueryData(queryResult, to, groupByDim);
		}
		Set<String> userIds = new HashSet<>();
		for(Map resultMap : queryResult){
			Map<String, Object> eventData = (Map<String, Object>)resultMap.get("event");
			userIds.add(eventData.get(groupByDim).toString());
		}

		return userIds;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getBroker() {
		return broker;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getGroupByDim() {
		return groupByDim;
	}


	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public QueryUpdateBean getTo() {
		return to;
	}
}
