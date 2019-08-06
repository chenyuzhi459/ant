package io.sugo.services.usergroup.model;

import com.fasterxml.jackson.annotation.*;
import io.sugo.common.redis.RedisInfo;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.common.utils.QueryUtil;
import io.sugo.services.tag.model.QueryUpdateBean;
import io.sugo.services.usergroup.model.query.Query;
import io.sugo.services.usergroup.model.query.UserGroupQuery;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;




public class UserGroupBean extends GroupBean{
	public static final Set<String> INDEX_TYPES = new HashSet<>(Arrays.asList("tindex","uindex"));
	private static final String TYPE="usergroup";
	public static final String FINAL_GROUP_TYPE = "finalGroup";
//	protected  String type;
//	protected  String broker;
//	protected Query query;      //maybe userGroupQuery or groupByQuery
	protected  String op;        // used for AssistantGroup



	@JsonCreator
	public UserGroupBean(
			@JsonProperty("type") String type,
			@JsonProperty("query") Query query,
			@JsonProperty("op") String op


	) {

		super(type, query);
//		this.broker = broker;
		this.op = op == null ? "" : op;


	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getType() {
		return TYPE;
	}

//	@JsonProperty
//	@JsonInclude(JsonInclude.Include.NON_NULL)
//	public String getBroker() {
//		return broker;
//	}

//	@JsonProperty
//	public Query getQuery() {
//		return query;
//	}

	@JsonProperty
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getOp() {
		return op;
	}



	public  Set<String>  getData(Map<RedisInfo, Set<String>> tempGroups){
		UserGroupSerDeserializer serDeserializer = new UserGroupSerDeserializer(query.getDataConfig());
		Set<String> userIds = new HashSet<>();
		serDeserializer.deserialize(userIds);
		return userIds;
	}

}
