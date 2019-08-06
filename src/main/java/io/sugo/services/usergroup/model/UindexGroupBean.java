package io.sugo.services.usergroup.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.sugo.common.redis.RedisInfo;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.services.tag.model.QueryUpdateBean;
import io.sugo.services.usergroup.model.query.Query;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenyuzhi on 19-8-5.
 */
public class UindexGroupBean extends UserGroupBean {
	private static final String TYPE="uindex";
	protected  String broker;
	protected String op;
	@JsonCreator
	public UindexGroupBean(
			@JsonProperty("type") String type,
			@JsonProperty("broker") String broker,
			@JsonProperty("query") Query query,
			@JsonProperty("op") String op

	) {
		super(type,query,op);
		Preconditions.checkNotNull(broker, "broker can not be null.");
		this.broker = broker;
		this.op = op;
	}


	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getType() {
		return TYPE;
	}

	@Override
	public  Set<String>  getData(Map<RedisInfo, Set<String>> tempGroups) {
		UserGroupSerDeserializer serDeserializer = new UserGroupSerDeserializer(query.getDataConfig());
		Set<String> userIds = new HashSet<>();
		serDeserializer.deserialize(userIds);
		//加入临时分群map, TODO 后续统一在close处理
		tempGroups.computeIfAbsent(query.getDataConfig().getRedisInfo(), k -> new HashSet<>())
				.add(query.getDataConfig().getGroupId());
		return userIds;
	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getBroker() {
		return broker;
	}

}
